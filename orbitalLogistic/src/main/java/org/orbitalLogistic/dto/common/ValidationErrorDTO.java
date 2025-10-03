package org.orbitalLogistic.dto.common;

import jakarta.validation.constraints.NotNull;

public record ValidationErrorDTO(
        @NotNull String field,
        @NotNull String message,
        Object rejectedValue
) {}