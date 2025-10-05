package org.orbitalLogistic.dto.response;

import org.orbitalLogistic.entities.enums.ManifestStatus;
import org.orbitalLogistic.entities.enums.ManifestPriority;

import java.time.LocalDateTime;

public record CargoManifestResponseDTO(
    Long id,
    String spacecraftName,
    String cargoName,
    String storageUnitCode,
    Integer quantity,
    ManifestStatus manifestStatus,
    ManifestPriority priority,
    LocalDateTime loadedAt,
    LocalDateTime unloadedAt,
    String loadedByUserName,
    String unloadedByUserName
) {}
