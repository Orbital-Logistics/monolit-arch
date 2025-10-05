package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;
import org.orbitalLogistic.entities.enums.AssignmentRole;

import java.util.List;

public record MissionAssignmentRequestDTO(
    @NotNull(message = "Mission ID is required")
    Long missionId,

    @NotNull(message = "User ID is required")
    Long userId,

    @NotNull(message = "Assignment role is required")
    AssignmentRole assignmentRole,

    @Size(max = 100, message = "Responsibility zone must not exceed 100 characters")
    String responsibilityZone,

    // Поля для массовых назначений экипажа
    List<CrewAssignmentDTO> crewAssignments
) {
    public record CrewAssignmentDTO(
        @NotNull Long userId,
        @NotNull AssignmentRole assignmentRole,
        @Size(max = 100) String responsibilityZone
    ) {}
}
