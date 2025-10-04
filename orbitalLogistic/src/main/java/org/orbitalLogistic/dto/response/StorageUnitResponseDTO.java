package org.orbitalLogistic.dto.response;

import org.orbitalLogistic.entities.enums.StorageType;

import java.math.BigDecimal;

public record StorageUnitResponseDTO(
    Long id,
    String unitCode,
    String location,
    StorageType storageType,
    BigDecimal totalMassCapacity,
    BigDecimal totalVolumeCapacity,
    BigDecimal currentMass,
    BigDecimal currentVolume,
    BigDecimal availableMassCapacity,
    BigDecimal availableVolumeCapacity,
    Double massUsagePercentage,
    Double volumeUsagePercentage
) {}
