package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequestDTO(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 100) String username,
        @NotBlank @Size(min = 8) String password
) {}
