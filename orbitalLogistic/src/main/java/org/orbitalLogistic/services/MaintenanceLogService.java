package org.orbitalLogistic.services;

import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.dto.response.MaintenanceLogResponseDTO;
import org.orbitalLogistic.entities.MaintenanceLog;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.enums.MaintenanceStatus;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.exceptions.MaintenanceLogNotFoundException;
import org.orbitalLogistic.exceptions.user.UserNotFoundException;
import org.orbitalLogistic.mappers.MaintenanceLogMapper;
import org.orbitalLogistic.repositories.MaintenanceLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaintenanceLogService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final MaintenanceLogMapper maintenanceLogMapper;

    private SpacecraftService spacecraftService;
    private UserService userService;

    public MaintenanceLogService(MaintenanceLogRepository maintenanceLogRepository,
                                MaintenanceLogMapper maintenanceLogMapper) {
        this.maintenanceLogRepository = maintenanceLogRepository;
        this.maintenanceLogMapper = maintenanceLogMapper;
    }

    @Autowired
    public void setSpacecraftService(@Lazy SpacecraftService spacecraftService) {
        this.spacecraftService = spacecraftService;
    }

    @Autowired
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    public PageResponseDTO<MaintenanceLogResponseDTO> getAllMaintenanceLogs(int page, int size) {
        long total = maintenanceLogRepository.count();
        List<MaintenanceLog> logs = (List<MaintenanceLog>) maintenanceLogRepository.findAll();

        List<MaintenanceLogResponseDTO> logDTOs = logs.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(logDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public PageResponseDTO<MaintenanceLogResponseDTO> getSpacecraftMaintenanceHistory(Long spacecraftId, int page, int size) {
        List<MaintenanceLog> logs = maintenanceLogRepository.findBySpacecraftIdOrderByStartTime(spacecraftId);

        List<MaintenanceLogResponseDTO> logDTOs = logs.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) logs.size() / size);
        return new PageResponseDTO<>(logDTOs, page, size, logs.size(), totalPages, page == 0, page >= totalPages - 1);
    }

    /**
     * Требует @Transactional, так как кроме создания записи о техобслуживании
     * может обновлять статус космического корабля. Обе операции должны выполниться атомарно.
     */
    @Transactional
    public MaintenanceLogResponseDTO createMaintenanceLog(MaintenanceLogRequestDTO request) {
        validateEntities(request);

        MaintenanceLog maintenanceLog = maintenanceLogMapper.toEntity(request);

        if (request.startTime() != null && request.startTime().isBefore(LocalDateTime.now().plusMinutes(5))) {
            spacecraftService.updateSpacecraftStatus(request.spacecraftId(), SpacecraftStatus.MAINTENANCE);
        }

        MaintenanceLog saved = maintenanceLogRepository.save(maintenanceLog);
        return toResponseDTO(saved);
    }

    /**
     * Требует @Transactional, так как обновляет запись техобслуживания
     * и может изменять статус космического корабля. Обе операции должны быть атомарными.
     */
    @Transactional
    public MaintenanceLogResponseDTO updateMaintenanceStatus(Long id, MaintenanceLogRequestDTO request) {
        MaintenanceLog maintenanceLog = maintenanceLogRepository.findById(id)
                .orElseThrow(() -> new MaintenanceLogNotFoundException("Maintenance log not found with id: " + id));

        if (request.endTime() != null) {
            maintenanceLog.setEndTime(request.endTime());
        }

        if (request.finalCost() != null) {
            maintenanceLog.setCost(request.finalCost());
        }

        if (request.completionNotes() != null) {
            maintenanceLog.setDescription(request.completionNotes());
        }

        if (request.newSpacecraftStatus() != null) {
            maintenanceLog.setStatus(MaintenanceStatus.COMPLETED);
            spacecraftService.updateSpacecraftStatus(maintenanceLog.getSpacecraftId(), request.newSpacecraftStatus());
        }

        MaintenanceLog updated = maintenanceLogRepository.save(maintenanceLog);
        return toResponseDTO(updated);
    }

    private void validateEntities(MaintenanceLogRequestDTO request) {
        spacecraftService.getEntityById(request.spacecraftId());
        userService.getEntityById(request.performedByUserId());

        if (request.supervisedByUserId() != null) {
            userService.getEntityById(request.supervisedByUserId());
        }

        if (request.completedByUserId() != null) {
            userService.getEntityById(request.completedByUserId());
        }
    }

    private MaintenanceLogResponseDTO toResponseDTO(MaintenanceLog maintenanceLog) {
        Spacecraft spacecraft = spacecraftService.getEntityById(maintenanceLog.getSpacecraftId());
        try {
            User performedByUser = userService.getEntityById(maintenanceLog.getPerformedByUserId());
            String supervisedByUserName = null;
            if (maintenanceLog.getSupervisedByUserId() != null) {
                try {
                    User supervisedByUser = userService.getEntityByIdOrNull(maintenanceLog.getSupervisedByUserId());
                    if (supervisedByUser != null) {
                        supervisedByUserName = supervisedByUser.getUsername();
                        return maintenanceLogMapper.toResponseDTO(maintenanceLog,
                                spacecraft.getName(),
                                performedByUser.getUsername(),
                                supervisedByUserName);
                    }
                } catch (UserNotFoundException e) {
                    throw new UserNotFoundException("supervisedByUser not found with id: " + maintenanceLog.getSupervisedByUserId());
                }

            }
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("performedByUser not found with id: " + maintenanceLog.getPerformedByUserId());
        }


        return null;
    }
}
