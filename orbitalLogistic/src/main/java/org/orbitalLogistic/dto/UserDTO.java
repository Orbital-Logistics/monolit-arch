package org.orbitalLogistic.dto;

import org.orbitalLogistic.entities.enums.UserRoles;

import lombok.Builder;

@Builder
public record UserDTO(Long id, String email, String username, UserRoles role) {}
