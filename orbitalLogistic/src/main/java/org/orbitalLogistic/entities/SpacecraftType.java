package org.orbitalLogistic.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.orbitalLogistic.entities.enums.SpacecraftClassification;

@Table("spacecraft_type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacecraftType {

    @Id
    private Long id;

    @NotBlank
    @Size(max = 50, message = "Type name must not exceed 50 characters")
    private String typeName;

    @NotNull
    private SpacecraftClassification classification;

    @Positive(message = "Max crew capacity must be positive")
    private Integer maxCrewCapacity;
}