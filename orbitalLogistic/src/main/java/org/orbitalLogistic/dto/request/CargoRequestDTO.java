package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;
import org.orbitalLogistic.entities.enums.*;

import java.math.BigDecimal;

public record CargoRequestDTO(
    @NotBlank
    @Size(max = 200)
    String name,

    @NotNull
    Long cargoCategoryId,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal massPerUnit,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal volumePerUnit,

    @NotNull
    CargoType cargoType,

    @NotNull
    HazardLevel hazardLevel
) {}