package org.orbitalLogistic.user.dto.common;

import java.util.List;

public record PageResponseDTO<T>(
    List<T> content,
    int currentPage,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}
