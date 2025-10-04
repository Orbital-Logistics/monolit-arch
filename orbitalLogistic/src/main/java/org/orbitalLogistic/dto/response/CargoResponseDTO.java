package org.orbitalLogistic.dto.response;

import org.orbitalLogistic.entities.enums.CargoType;
import org.orbitalLogistic.entities.enums.HazardLevel;

import java.math.BigDecimal;

public record CargoResponseDTO(
    Long id,
    String name,
    String cargoCategoryName,
    BigDecimal massPerUnit,
    BigDecimal volumePerUnit,
    CargoType cargoType,
    HazardLevel hazardLevel,
    Integer totalQuantity
) {}
