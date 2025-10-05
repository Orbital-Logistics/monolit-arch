package org.orbitalLogistic.dto.response;

import java.util.List;

public record CargoCategoryResponseDTO(
    Long id,
    String name,
    Long parentCategoryId,
    String parentCategoryName,
    String description,
    List<CargoCategoryResponseDTO> children,
    Integer level
) {}
