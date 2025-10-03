package org.orbitalLogistic.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private Long id;

    @NotBlank
    @Email(message = "Email must be valid")
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 2, max = 64, message = "Name must be between 2 and 64 characters")
    @Pattern(regexp = "^[a-zA-Zа-яА-Я\\s]+$", message = "Name can only contain letters and spaces")
    private String username;

    @NotBlank
    @Size(min = 2, max = 100)
    private String first_name;

    @NotBlank
    @Size(min = 2, max = 100)
    private String last_name;

    @NotNull
    private Long roleId;

    @NotBlank
    @Size(min = 8, max = 255, message = "Password must be at least 8 characters")
    private String passwordHash;
}
