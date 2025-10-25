package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;
import org.orbitalLogistic.entities.enums.MissionType;
import org.orbitalLogistic.entities.enums.MissionStatus;
import org.orbitalLogistic.entities.enums.MissionPriority;

import java.time.LocalDateTime;
import java.util.List;

public record MissionRequestDTO(
    @NotBlank(message = "Mission code is required")
    @Size(max = 20, message = "Mission code must not exceed 20 characters")
    String missionCode,

    @NotBlank(message = "Mission name is required")
    @Size(max = 200, message = "Mission name must not exceed 200 characters")
    String missionName,

    @NotNull(message = "Mission type is required")
    MissionType missionType,

    @NotNull(message = "Mission status is required")
    MissionStatus status, // Добавлено недостающее поле

    @NotNull(message = "Priority is required")
    MissionPriority priority,

    @NotNull(message = "Commanding officer is required")
    Long commandingOfficerId,

    @NotNull(message = "Spacecraft is required")
    Long spacecraftId,

    LocalDateTime scheduledArrival,

    LocalDateTime actualArrival,
    Long completedByUserId,
    String completionNotes,
    Boolean isSuccessful,
    String failureReason,

    List<CargoItemDTO> cargoItems
) {
    public record CargoItemDTO(
        @NotNull Long cargoId,
        @NotNull Long storageUnitId,
        @NotNull @Min(1) Integer quantity
    ) {}
}