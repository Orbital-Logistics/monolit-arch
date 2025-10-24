package org.orbitalLogistic.dto.response;

import jakarta.validation.constraints.NotNull;

public record UserResponseDTO(
        @NotNull Long id,
        @NotNull String email,
        @NotNull String username
) {}
