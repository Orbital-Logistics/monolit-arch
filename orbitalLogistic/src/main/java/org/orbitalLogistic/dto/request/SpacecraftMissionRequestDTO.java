package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;

public record SpacecraftMissionRequestDTO(
    @NotNull(message = "Spacecraft ID is required")
    Long spacecraftId,

    @NotNull(message = "Mission ID is required")
    Long missionId,

    @Size(max = 100, message = "Role description must not exceed 100 characters")
    String roleDescription,

    @NotNull(message = "Assigned by user ID is required")
    Long assignedByUserId
) {}
