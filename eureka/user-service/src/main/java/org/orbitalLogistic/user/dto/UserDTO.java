package org.orbitalLogistic.user.dto;

import org.orbitalLogistic.user.entities.enums.UserRoles;

import lombok.Builder;

@Builder
public record UserDTO(Long id, String email, String username, UserRoles role) {}
