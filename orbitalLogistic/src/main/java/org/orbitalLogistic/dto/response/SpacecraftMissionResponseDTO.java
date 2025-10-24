package org.orbitalLogistic.dto.response;

public record SpacecraftMissionResponseDTO(
    Long spacecraftId,
    String spacecraftName,
    String registryCode,
    Long missionId,
    String missionName,
    String missionCode,
    String roleDescription,
    String assignedByUserName
) {}
