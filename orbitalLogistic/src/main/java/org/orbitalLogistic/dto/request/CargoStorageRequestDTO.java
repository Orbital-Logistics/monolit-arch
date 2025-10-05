package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;

public record CargoStorageRequestDTO(
    @NotNull(message = "Storage unit ID is required")
    Long storageUnitId,

    @NotNull(message = "Cargo ID is required")
    Long cargoId,

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    Integer quantity,

    // Поля для обновления количества
    Long updatedByUserId,

    @Size(max = 100, message = "Reason must not exceed 100 characters")
    String reason,

    String notes
) {}
