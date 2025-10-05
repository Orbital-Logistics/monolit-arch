package org.orbitalLogistic.dto.response;

import org.orbitalLogistic.entities.enums.SpacecraftClassification;

public record SpacecraftTypeResponseDTO(
    Long id,
    String typeName,
    SpacecraftClassification classification,
    Integer maxCrewCapacity
) {}
