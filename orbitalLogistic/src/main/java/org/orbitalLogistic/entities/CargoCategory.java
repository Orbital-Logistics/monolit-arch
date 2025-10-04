package org.orbitalLogistic.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("cargo_category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoCategory {

    @Id
    private Long id;

    @NotBlank
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private Long parentCategoryId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}