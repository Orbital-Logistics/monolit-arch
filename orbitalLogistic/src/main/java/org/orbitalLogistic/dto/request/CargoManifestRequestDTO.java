package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;
import org.orbitalLogistic.entities.enums.ManifestPriority;

import java.time.LocalDateTime;
import java.util.List;

public record CargoManifestRequestDTO(
    @NotNull(message = "Spacecraft ID is required")
    Long spacecraftId,

    @NotNull(message = "Cargo ID is required")
    Long cargoId,

    @NotNull(message = "Storage unit ID is required")
    Long storageUnitId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be positive")
    Integer quantity,

    @NotNull(message = "Priority is required")
    ManifestPriority priority,

    @NotNull(message = "Loaded by user ID is required")
    Long loadedByUserId,

    // Поля для массовых операций загрузки/выгрузки
    List<CargoItemDTO> cargoItems,
    Long targetStorageUnitId,
    Long unloadedByUserId,
    LocalDateTime loadedAt,
    LocalDateTime unloadedAt,
    String operation, // "LOAD" или "UNLOAD"
    String notes
) {
    public record CargoItemDTO(
        @NotNull Long cargoId,
        @NotNull Long storageUnitId,
        @NotNull @Min(1) Integer quantity,
        ManifestPriority priority
    ) {}
}
