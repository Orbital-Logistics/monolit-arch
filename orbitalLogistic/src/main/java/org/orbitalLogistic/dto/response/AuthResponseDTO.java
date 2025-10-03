package org.orbitalLogistic.dto.response;

public record AuthResponseDTO(
        String token,
        String type, // "Bearer"
        UserResponseDTO user
) {}
