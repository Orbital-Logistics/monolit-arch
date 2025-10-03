package org.orbitalLogistic.dto.common;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PageResponseDTO<T>(
        @NotNull List<T> content,
        @NotNull Integer page,
        @NotNull Integer size,
        @NotNull Long totalElements,
        @NotNull Integer totalPages,
        @NotNull Boolean first,
        @NotNull Boolean last
) {}
