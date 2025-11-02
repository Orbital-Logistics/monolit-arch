package org.orbitalLogistic.services;

import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MissionRequestDTO;
import org.orbitalLogistic.dto.response.MissionResponseDTO;
import org.orbitalLogistic.entities.Mission;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.enums.MissionPriority;
import org.orbitalLogistic.entities.enums.MissionStatus;
import org.orbitalLogistic.entities.enums.MissionType;
import org.orbitalLogistic.exceptions.MissionNotFoundException;
import org.orbitalLogistic.exceptions.user.UserNotFoundException;
import org.orbitalLogistic.exceptions.MissionAlreadyExistsException;
import org.orbitalLogistic.mappers.MissionMapper;
import org.orbitalLogistic.repositories.MissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionMapper missionMapper;
    private final JdbcTemplate jdbcTemplate;

    private UserService userService;
    private SpacecraftService spacecraftService;

    public MissionService(MissionRepository missionRepository,
                         MissionMapper missionMapper,
                         JdbcTemplate jdbcTemplate) {
        this.missionRepository = missionRepository;
        this.missionMapper = missionMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setSpacecraftService(@Lazy SpacecraftService spacecraftService) {
        this.spacecraftService = spacecraftService;
    }

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

    public MissionResponseDTO getMissionById(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));
        return toResponseDTO(mission);
    }

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

        userService.getEntityById(request.commandingOfficerId());
        spacecraftService.getEntityById(request.spacecraftId());

        String sql = "INSERT INTO mission " +
                    "(mission_code, mission_name, mission_type, status, priority, " +
                    "commanding_officer_id, spacecraft_id, scheduled_departure, scheduled_arrival) " +
                    "VALUES (?, ?, ?::mission_type_enum, ?::mission_status_enum, ?::mission_priority_enum, " +
                    "?, ?, ?, ?) " +
                    "RETURNING id";
        
        Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                request.missionCode(),
                request.missionName(),
                request.missionType().name(),
                request.status().name(),
                request.priority().name(),    
                request.commandingOfficerId(),
                request.spacecraftId(),
                request.scheduledDeparture(), 
                request.scheduledArrival() 
        );

        String selectSql = "SELECT * FROM mission WHERE id = ?";
        Mission saved = jdbcTemplate.queryForObject(selectSql, 
            (rs, rowNum) -> Mission.builder()
                    .id(rs.getLong("id"))
                    .missionCode(rs.getString("mission_code"))
                    .missionName(rs.getString("mission_name"))
                    .missionType(MissionType.valueOf(rs.getString("mission_type")))
                    .status(MissionStatus.valueOf(rs.getString("status")))
                    .priority(MissionPriority.valueOf(rs.getString("priority")))
                    .commandingOfficerId(rs.getLong("commanding_officer_id"))
                    .spacecraftId(rs.getLong("spacecraft_id"))
                    .scheduledDeparture(rs.getTimestamp("scheduled_departure").toLocalDateTime())
                    .scheduledArrival(rs.getTimestamp("scheduled_arrival") != null ? 
                        rs.getTimestamp("scheduled_arrival").toLocalDateTime() : null)
                    .build(),
            newId);

        return toResponseDTO(saved);
    }

    public MissionResponseDTO updateMission(Long id, MissionRequestDTO request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        if (!mission.getMissionCode().equals(request.missionCode()) &&
            missionRepository.existsByMissionCode(request.missionCode())) {
            throw new MissionAlreadyExistsException("Mission with code already exists: " + request.missionCode());
        }


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
                request.missionType().name(),
                request.status().name(),
                request.priority().name(),
                userService.findUserById(request.commandingOfficerId()).id(),
                spacecraftService.getSpacecraftById(request.spacecraftId()).id(),
                request.scheduledDeparture(),
                request.scheduledArrival(),
                id);

        mission.setMissionCode(request.missionCode());
        mission.setMissionName(request.missionName());
        mission.setMissionType(request.missionType());
        mission.setStatus(request.status());
        mission.setPriority(request.priority());
        mission.setCommandingOfficerId(userService.findUserById(request.commandingOfficerId()).id());
        mission.setSpacecraftId(spacecraftService.getSpacecraftById(request.spacecraftId()).id());
        mission.setScheduledDeparture(request.scheduledDeparture());
        mission.setScheduledArrival(request.scheduledArrival());

        return toResponseDTO(mission);
    }

    public MissionResponseDTO completeMission(Long id, MissionRequestDTO request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        String sql = "UPDATE mission SET status = ?::mission_status_enum WHERE id = ?";
        jdbcTemplate.update(sql, 
            request.isSuccessful() ? MissionStatus.COMPLETED.name() : MissionStatus.CANCELLED.name(), 
            id
        );

        mission.setStatus(request.isSuccessful() ? MissionStatus.COMPLETED : MissionStatus.CANCELLED);

        return toResponseDTO(mission);
    }


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

        return toResponseDTO(mission);
    }

    public List<MissionResponseDTO> getMissionsByStatus(MissionStatus status) {
        return missionRepository.findByStatus(status).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public Mission getEntityById(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));
    }

    private MissionResponseDTO toResponseDTO(Mission mission) {
        User commandingOfficer = userService.getEntityById(mission.getCommandingOfficerId());
        Spacecraft spacecraft = spacecraftService.getEntityById(mission.getSpacecraftId());

        return missionMapper.toResponseDTO(mission,
                commandingOfficer.getUsername(),
                spacecraft.getName());
    }

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
