package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;

public record CargoCategoryRequestDTO(
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name,

    Long parentCategoryId,

    String description
) {}
