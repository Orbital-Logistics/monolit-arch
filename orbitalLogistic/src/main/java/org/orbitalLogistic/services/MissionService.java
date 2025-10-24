package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MissionRequestDTO;
import org.orbitalLogistic.dto.response.MissionResponseDTO;
import org.orbitalLogistic.entities.Mission;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.enums.MissionStatus;
import org.orbitalLogistic.exceptions.MissionNotFoundException;
import org.orbitalLogistic.exceptions.MissionAlreadyExistsException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.MissionMapper;
import org.orbitalLogistic.repositories.MissionRepository;
import org.orbitalLogistic.repositories.UserRepository;
import org.orbitalLogistic.repositories.SpacecraftRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final SpacecraftRepository spacecraftRepository;
    private final MissionMapper missionMapper;
    private final JdbcTemplate jdbcTemplate; // Добавляем JdbcTemplate

    @Transactional(readOnly = true)
    public PageResponseDTO<MissionResponseDTO> getMissions(String missionCode, String status, String missionType, int page, int size) {
        int offset = page * size;
        List<Mission> missions = missionRepository.findWithFilters(missionCode, null, status, missionType, null, null, null, size, offset);
        long total = missionRepository.countWithFilters(missionCode, null, status, missionType, null, null, null);

        List<MissionResponseDTO> missionDTOs = missions.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(missionDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    @Transactional(readOnly = true)
    public MissionResponseDTO getMissionById(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));
        return toResponseDTO(mission);
    }

    @Transactional(readOnly = true)
    public List<MissionResponseDTO> getActiveMissions() {
        LocalDateTime now = LocalDateTime.now();
        return missionRepository.findActiveMissions(now).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public MissionResponseDTO createMission(MissionRequestDTO request) {
        if (missionRepository.existsByMissionCode(request.missionCode())) {
            throw new MissionAlreadyExistsException("Mission with code already exists: " + request.missionCode());
        }

        // Validate commanding officer exists
        User commandingOfficer = userRepository.findById(request.commandingOfficerId())
                .orElseThrow(() -> new DataNotFoundException("Commanding officer not found"));

        // Validate spacecraft exists
        Spacecraft spacecraft = spacecraftRepository.findById(request.spacecraftId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found"));

        // Используем JdbcTemplate для создания с явным CAST enum
        String sql = "INSERT INTO mission " +
                     "(mission_code, mission_name, mission_type, status, priority, " +
                     "commanding_officer_id, spacecraft_id, scheduled_departure, scheduled_arrival) " +
                     "VALUES (?, ?, ?::mission_type_enum, ?::mission_status_enum, ?::mission_priority_enum, " +
                     "?, ?, ?, ?) " +
                     "RETURNING id";
        
        Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                request.missionCode(),
                request.missionName(),
                request.missionType().name(),     // mission_status_enum
                request.status().name(),
                request.priority().name(),    // mission_priority_enum
                request.commandingOfficerId(),
                request.spacecraftId(),
                request.scheduledArrival()
        );

        // Получаем созданную запись
        Mission saved = missionRepository.findById(newId)
                .orElseThrow(() -> new DataNotFoundException("Failed to create mission"));

        return toResponseDTO(saved);
    }

    public MissionResponseDTO updateMission(Long id, MissionRequestDTO request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        if (!mission.getMissionCode().equals(request.missionCode()) &&
            missionRepository.existsByMissionCode(request.missionCode())) {
            throw new MissionAlreadyExistsException("Mission with code already exists: " + request.missionCode());
        }

        // Используем JdbcTemplate для обновления с явным CAST enum
        String sql = "UPDATE mission SET " +
                     "mission_code = ?, " +
                     "mission_name = ?, " +
                     "mission_type = ?::mission_type_enum, " +
                     "status = ?::mission_status_enum, " +
                     "priority = ?::mission_priority_enum, " +
                     "commanding_officer_id = ?, " +
                     "spacecraft_id = ?, " +
                     "scheduled_departure = ?, " +
                     "scheduled_arrival = ? " +
                     "WHERE id = ?";
        
        jdbcTemplate.update(sql,
                request.missionCode(),
                request.missionName(),
                request.missionType().name(), // mission_type_enum
                request.status().name(),      // mission_status_enum
                request.priority().name(),    // mission_priority_enum
                request.commandingOfficerId(),
                request.spacecraftId(),
                request.scheduledArrival(),
                id
        );

        // Обновляем объект для возврата
        mission.setMissionCode(request.missionCode());
        mission.setMissionName(request.missionName());
        mission.setMissionType(request.missionType());
        mission.setStatus(request.status());
        mission.setPriority(request.priority());
        mission.setCommandingOfficerId(request.commandingOfficerId());
        mission.setSpacecraftId(request.spacecraftId());
        mission.setScheduledArrival(request.scheduledArrival());

        return toResponseDTO(mission);
    }

    @Transactional
    public MissionResponseDTO completeMission(Long id, MissionRequestDTO request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        // Используем JdbcTemplate для завершения миссии
        String sql = "UPDATE mission SET status = ?::mission_status_enum WHERE id = ?";
        jdbcTemplate.update(sql, 
            request.isSuccessful() ? MissionStatus.COMPLETED.name() : MissionStatus.CANCELLED.name(), 
            id
        );

        // Обновляем объект
        mission.setStatus(request.isSuccessful() ? MissionStatus.COMPLETED : MissionStatus.CANCELLED);

        return toResponseDTO(mission);
    }

    // Дополнительные методы для бизнес-логики

    public MissionResponseDTO startMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        String sql = "UPDATE mission SET status = ?::mission_status_enum WHERE id = ?";
        jdbcTemplate.update(sql, MissionStatus.IN_PROGRESS.name(), id);

        mission.setStatus(MissionStatus.IN_PROGRESS);
        return toResponseDTO(mission);
    }

    public MissionResponseDTO scheduleMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        String sql = "UPDATE mission SET status = ?::mission_status_enum WHERE id = ?";
        jdbcTemplate.update(sql, MissionStatus.SCHEDULED.name(), id);

        mission.setStatus(MissionStatus.SCHEDULED);
        return toResponseDTO(mission);
    }

    public MissionResponseDTO cancelMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        String sql = "UPDATE mission SET status = ?::mission_status_enum WHERE id = ?";
        jdbcTemplate.update(sql, MissionStatus.CANCELLED.name(), id);

        mission.setStatus(MissionStatus.CANCELLED);
        return toResponseDTO(mission);
    }

    public MissionResponseDTO changeMissionPriority(Long id, String priority) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        String sql = "UPDATE mission SET priority = ?::mission_priority_enum WHERE id = ?";
        jdbcTemplate.update(sql, priority, id);

        // Обновляем объект (нужно преобразовать String в enum)
        // mission.setPriority(...);

        return toResponseDTO(mission);
    }

    @Transactional(readOnly = true)
    public List<MissionResponseDTO> getMissionsByStatus(MissionStatus status) {
        return missionRepository.findByStatus(status).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private MissionResponseDTO toResponseDTO(Mission mission) {
        User commandingOfficer = userRepository.findById(mission.getCommandingOfficerId())
                .orElseThrow(() -> new DataNotFoundException("Commanding officer not found"));

        Spacecraft spacecraft = spacecraftRepository.findById(mission.getSpacecraftId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found"));

        return missionMapper.toResponseDTO(mission,
                commandingOfficer.getUsername(),
                spacecraft.getName());
    }

    // Метод для проверки возможности создания миссии
    public boolean canCreateMission(Long spacecraftId, LocalDateTime departure, LocalDateTime arrival) {
        String sql = "SELECT COUNT(*) = 0 FROM mission " +
                     "WHERE spacecraft_id = ? " +
                     "AND status IN ('SCHEDULED', 'IN_PROGRESS') " +
                     "AND (scheduled_departure, scheduled_arrival) OVERLAPS (?, ?)";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, 
                spacecraftId, departure, arrival);
        
        return count != null && count == 0;
    }
}