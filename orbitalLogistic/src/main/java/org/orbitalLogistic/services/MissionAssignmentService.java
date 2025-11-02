package org.orbitalLogistic.services;

import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.dto.response.MissionAssignmentResponseDTO;
import org.orbitalLogistic.entities.MissionAssignment;
import org.orbitalLogistic.entities.Mission;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.enums.AssignmentRole;
import org.orbitalLogistic.exceptions.MissionAssignmentNotFoundException;
import org.orbitalLogistic.exceptions.UserAlreadyAssignedException;
import org.orbitalLogistic.mappers.MissionAssignmentMapper;
import org.orbitalLogistic.repositories.MissionAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MissionAssignmentService {

    private final MissionAssignmentRepository missionAssignmentRepository;
    private final MissionAssignmentMapper missionAssignmentMapper;
    private final JdbcTemplate jdbcTemplate;

    private MissionService missionService;
    private UserService userService;

    public MissionAssignmentService(MissionAssignmentRepository missionAssignmentRepository,
                                   MissionAssignmentMapper missionAssignmentMapper,
                                   JdbcTemplate jdbcTemplate) {
        this.missionAssignmentRepository = missionAssignmentRepository;
        this.missionAssignmentMapper = missionAssignmentMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    public void setMissionService(@Lazy MissionService missionService) {
        this.missionService = missionService;
    }

    @Autowired
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    public PageResponseDTO<MissionAssignmentResponseDTO> getAllAssignments(int page, int size) {
        long total = missionAssignmentRepository.count();
        List<MissionAssignment> assignments = (List<MissionAssignment>) missionAssignmentRepository.findAll();

        List<MissionAssignmentResponseDTO> assignmentDTOs = assignments.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(assignmentDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public PageResponseDTO<MissionAssignmentResponseDTO> getMissionAssignments(Long missionId, int page, int size) {
        List<MissionAssignment> assignments = missionAssignmentRepository.findByMissionIdOrderByRole(missionId);

        List<MissionAssignmentResponseDTO> assignmentDTOs = assignments.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) assignments.size() / size);
        return new PageResponseDTO<>(assignmentDTOs, page, size, assignments.size(), totalPages, page == 0, page >= totalPages - 1);
    }

    /**
     * Требует @Transactional, так как может создавать множественные назначения экипажа
     * в одной операции. Все назначения должны быть сохранены атомарно.
     */
    @Transactional
    public List<MissionAssignmentResponseDTO> assignCrew(Long missionId, MissionAssignmentRequestDTO request) {
        missionService.getEntityById(missionId);

        List<MissionAssignmentResponseDTO> results = List.of();

        if (request.userId() != null) {
            MissionAssignment assignment = createAssignment(missionId, request.userId(),
                    request.assignmentRole(), request.responsibilityZone());
            results = List.of(toResponseDTO(assignment));
        }

        if (request.crewAssignments() != null && !request.crewAssignments().isEmpty()) {
            results = request.crewAssignments().stream()
                    .map(crewDto -> createAssignment(missionId, crewDto.userId(),
                            crewDto.assignmentRole(), crewDto.responsibilityZone()))
                    .map(this::toResponseDTO)
                    .toList();
        }

        return results;
    }

    public void removeAssignment(Long assignmentId) {
        if (!missionAssignmentRepository.existsById(assignmentId)) {
            throw new MissionAssignmentNotFoundException("Assignment not found with id: " + assignmentId);
        }
        missionAssignmentRepository.deleteById(assignmentId);
    }

    private MissionAssignment createAssignment(Long missionId, Long userId,
        org.orbitalLogistic.entities.enums.AssignmentRole role, String responsibilityZone) {

        User user = userService.getEntityById(userId);

        if (missionAssignmentRepository.existsByMissionIdAndUserId(missionId, userId)) {
                throw new UserAlreadyAssignedException(missionId, userId, user.getUsername());
        }

        String sql = "INSERT INTO mission_assignment " +
                        "(mission_id, user_id, assignment_role, responsibility_zone) " +
                        "VALUES (?, ?, ?::assignment_role_enum, ?) " +
                        "RETURNING id";
        
        Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                missionId,
                userId,
                role.name(),
                responsibilityZone
        );

        String selectSql = "SELECT * FROM mission_assignment WHERE id = ?";
        
        return jdbcTemplate.queryForObject(selectSql, 
                (rs, rowNum) -> MissionAssignment.builder()
                        .id(rs.getLong("id"))
                        .missionId(rs.getLong("mission_id"))
                        .userId(rs.getLong("user_id"))
                        .assignmentRole(AssignmentRole.valueOf(rs.getString("assignment_role")))
                        .responsibilityZone(rs.getString("responsibility_zone"))
                        .assignedAt(rs.getTimestamp("assigned_at").toLocalDateTime())
                        .build(),
                newId);
        }

    private MissionAssignmentResponseDTO toResponseDTO(MissionAssignment assignment) {
        Mission mission = missionService.getEntityById(assignment.getMissionId());
        User user = userService.getEntityById(assignment.getUserId());

        return missionAssignmentMapper.toResponseDTO(assignment,
                mission.getMissionName(),
                user.getUsername() + " " + user.getUsername());
    }
}
