package org.orbitalLogistic.dto.response;

import org.orbitalLogistic.entities.enums.AssignmentRole;

import java.time.LocalDateTime;

public record MissionAssignmentResponseDTO(
    Long id,
    Long missionId,
    String missionName,
    String userName,
    AssignmentRole assignmentRole,
    String responsibilityZone,
    LocalDateTime assignedAt
) {}
