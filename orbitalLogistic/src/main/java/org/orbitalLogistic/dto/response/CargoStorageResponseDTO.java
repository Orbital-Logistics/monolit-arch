package org.orbitalLogistic.dto.response;

import java.time.LocalDateTime;

public record CargoStorageResponseDTO(
    Long id,
    String storageUnitCode,
    String storageLocation,
    String cargoName,
    Integer quantity,
    LocalDateTime storedAt,
    LocalDateTime lastInventoryCheck,
    String lastCheckedByUserName
) {}
