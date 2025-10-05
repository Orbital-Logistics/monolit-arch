package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.dto.response.MissionAssignmentResponseDTO;
import org.orbitalLogistic.entities.MissionAssignment;
import org.orbitalLogistic.entities.Mission;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.exceptions.MissionAssignmentNotFoundException;
import org.orbitalLogistic.exceptions.MissionNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.MissionAssignmentMapper;
import org.orbitalLogistic.repositories.MissionAssignmentRepository;
import org.orbitalLogistic.repositories.MissionRepository;
import org.orbitalLogistic.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionAssignmentService {

    private final MissionAssignmentRepository missionAssignmentRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final MissionAssignmentMapper missionAssignmentMapper;

    @Transactional(readOnly = true)
    public PageResponseDTO<MissionAssignmentResponseDTO> getAllAssignments(int page, int size) {
        // Implementation using repository pagination
        // For simplicity, using a basic approach
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

    @Transactional(readOnly = true)
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

    @Transactional
    public List<MissionAssignmentResponseDTO> assignCrew(Long missionId, MissionAssignmentRequestDTO request) {
        // Validate mission exists
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + missionId));

        List<MissionAssignmentResponseDTO> results = List.of();

        // Handle individual assignment
        if (request.userId() != null) {
            MissionAssignment assignment = createAssignment(missionId, request.userId(),
                    request.assignmentRole(), request.responsibilityZone());
            results = List.of(toResponseDTO(assignment));
        }

        // Handle crew assignments
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

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + userId));

        // Check if assignment already exists
        if (missionAssignmentRepository.existsByMissionIdAndUserId(missionId, userId)) {
            throw new RuntimeException("User already assigned to this mission");
        }

        MissionAssignment assignment = MissionAssignment.builder()
                .missionId(missionId)
                .userId(userId)
                .assignmentRole(role)
                .responsibilityZone(responsibilityZone)
                .build();

        return missionAssignmentRepository.save(assignment);
    }

    private MissionAssignmentResponseDTO toResponseDTO(MissionAssignment assignment) {
        Mission mission = missionRepository.findById(assignment.getMissionId())
                .orElseThrow(() -> new MissionNotFoundException("Mission not found"));

        User user = userRepository.findById(assignment.getUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        return missionAssignmentMapper.toResponseDTO(assignment,
                mission.getMissionName(),
                user.getFirst_name() + " " + user.getLast_name());
    }
}
