package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;
import org.orbitalLogistic.entities.enums.MaintenanceType;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaintenanceLogRequestDTO(
    @NotNull(message = "Spacecraft ID is required")
    Long spacecraftId,

    @NotNull(message = "Maintenance type is required")
    MaintenanceType maintenanceType,

    @NotNull(message = "Performed by user ID is required")
    Long performedByUserId,

    Long supervisedByUserId,

    LocalDateTime startTime,
    LocalDateTime endTime,

    String description,

    @DecimalMin(value = "0.0", message = "Cost must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Cost format invalid")
    BigDecimal cost,

    SpacecraftStatus newSpacecraftStatus,
    BigDecimal finalCost,
    String completionNotes,
    Long completedByUserId
) {}
