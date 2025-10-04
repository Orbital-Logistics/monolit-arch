package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;
import org.orbitalLogistic.entities.enums.StorageType;

import java.math.BigDecimal;

public record StorageUnitRequestDTO(
    @NotBlank
    @Size(max = 20)
    String unitCode,

    @NotBlank
    @Size(max = 100)
    String location,

    @NotNull
    StorageType storageType,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal totalMassCapacity,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal totalVolumeCapacity
) {}
