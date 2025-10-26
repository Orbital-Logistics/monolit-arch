package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.dto.response.MaintenanceLogResponseDTO;
import org.orbitalLogistic.entities.MaintenanceLog;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.enums.MaintenanceStatus;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.exceptions.MaintenanceLogNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.MaintenanceLogMapper;
import org.orbitalLogistic.repositories.MaintenanceLogRepository;
import org.orbitalLogistic.repositories.SpacecraftRepository;
import org.orbitalLogistic.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceLogService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final SpacecraftRepository spacecraftRepository;
    private final UserRepository userRepository;
    private final MaintenanceLogMapper maintenanceLogMapper;

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
     * может обновлять статус космического корабля. Обе операции должны выполниться
     * атомарно.
     */
    @Transactional
    public MaintenanceLogResponseDTO createMaintenanceLog(MaintenanceLogRequestDTO request) {
        validateEntities(request);

        MaintenanceLog maintenanceLog = maintenanceLogMapper.toEntity(request);

        if (request.startTime() != null && request.startTime().isBefore(LocalDateTime.now().plusMinutes(5))) {
            updateSpacecraftStatus(request.spacecraftId(), SpacecraftStatus.MAINTENANCE);
        }

        MaintenanceLog saved = maintenanceLogRepository.save(maintenanceLog);
        return toResponseDTO(saved);
    }

    /**
     * Требует @Transactional, так как обновляет запись техобслуживания
     * и может изменять статус космического корабля. Обе операции должны
     * быть атомарными.
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
            updateSpacecraftStatus(maintenanceLog.getSpacecraftId(), request.newSpacecraftStatus());
        }

        MaintenanceLog updated = maintenanceLogRepository.save(maintenanceLog);
        return toResponseDTO(updated);
    }

    private void validateEntities(MaintenanceLogRequestDTO request) {
        spacecraftRepository.findById(request.spacecraftId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found"));

        userRepository.findById(request.performedByUserId())
                .orElseThrow(() -> new DataNotFoundException("Performed by user not found"));

        if (request.supervisedByUserId() != null) {
            userRepository.findById(request.supervisedByUserId())
                    .orElseThrow(() -> new DataNotFoundException("Supervised by user not found"));
        }

        if (request.completedByUserId() != null) {
            userRepository.findById(request.completedByUserId())
                    .orElseThrow(() -> new DataNotFoundException("Completed by user not found"));
        }
    }

    private void updateSpacecraftStatus(Long spacecraftId, SpacecraftStatus status) {
        Spacecraft spacecraft = spacecraftRepository.findById(spacecraftId)
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found"));

        spacecraft.setStatus(status);
        spacecraftRepository.save(spacecraft);
    }

    private MaintenanceLogResponseDTO toResponseDTO(MaintenanceLog maintenanceLog) {
        Spacecraft spacecraft = spacecraftRepository.findById(maintenanceLog.getSpacecraftId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found"));

        User performedByUser = userRepository.findById(maintenanceLog.getPerformedByUserId())
                .orElseThrow(() -> new DataNotFoundException("Performed by user not found"));

        String supervisedByUserName = null;
        if (maintenanceLog.getSupervisedByUserId() != null) {
            User supervisedByUser = userRepository.findById(maintenanceLog.getSupervisedByUserId()).orElse(null);
            if (supervisedByUser != null) {
                supervisedByUserName = supervisedByUser.getUsername();
            }
        }

        return maintenanceLogMapper.toResponseDTO(maintenanceLog,
                spacecraft.getName(),
                performedByUser.getUsername(),
                supervisedByUserName);
    }
}
