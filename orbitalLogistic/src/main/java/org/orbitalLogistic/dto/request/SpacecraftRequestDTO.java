package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;

import java.math.BigDecimal;

public record SpacecraftRequestDTO(
    @NotBlank
    @Size(max = 20)
    String registryCode,

    @NotBlank
    @Size(max = 100)
    String name,

    @NotNull
    Long spacecraftTypeId,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal massCapacity,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal volumeCapacity,

    SpacecraftStatus status,

    @Size(max = 100)
    String currentLocation
) {}