package org.orbitalLogistic.dto.common;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        @NotNull String message,
        @NotNull String code,
        @NotNull LocalDateTime timestamp,
        String path,
        Map<String, String> details
) {}
