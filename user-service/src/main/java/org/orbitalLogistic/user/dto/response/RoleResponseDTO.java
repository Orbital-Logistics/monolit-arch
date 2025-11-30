package org.orbitalLogistic.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoleResponseDTO(
    Long id,
    String name
) {}