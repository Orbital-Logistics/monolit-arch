package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;
import org.orbitalLogistic.entities.enums.SpacecraftClassification;

public record SpacecraftTypeRequestDTO(
    @NotBlank(message = "Type name is required")
    @Size(max = 50, message = "Type name must not exceed 50 characters")
    String typeName,

    @NotNull(message = "Classification is required")
    SpacecraftClassification classification,

    @Min(value = 1, message = "Max crew capacity must be positive")
    Integer maxCrewCapacity
) {}
