package org.orbitalLogistic.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

    @Id
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String name;
}
