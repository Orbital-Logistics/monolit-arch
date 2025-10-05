package org.orbitalLogistic.dto.response;

import org.orbitalLogistic.entities.enums.MissionType;
import org.orbitalLogistic.entities.enums.MissionStatus;
import org.orbitalLogistic.entities.enums.MissionPriority;

import java.time.LocalDateTime;

public record MissionResponseDTO(
    Long id,
    String missionCode,
    String missionName,
    MissionType missionType,
    MissionStatus status,
    MissionPriority priority,
    String commandingOfficerName,
    String spacecraftName,
    LocalDateTime scheduledDeparture,
    LocalDateTime scheduledArrival
) {}
