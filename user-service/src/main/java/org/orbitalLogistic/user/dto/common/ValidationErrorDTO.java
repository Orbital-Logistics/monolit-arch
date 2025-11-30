package org.orbitalLogistic.user.dto.common;

import jakarta.validation.constraints.NotNull;

public record ValidationErrorDTO(
        @NotNull String field,
        @NotNull String message,
        Object rejectedValue
) {}