package org.orbitalLogistic.entities;

import org.orbitalLogistic.entities.enums.SpacecraftStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("spacecraft")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Spacecraft {

    @Id
    private Long id;

    @NotBlank
    @Size(max = 20, message = "Registry code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Registry code can only contain uppercase letters, numbers and hyphens")
    private String registryCode;

    @NotBlank
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Spacecraft type is required")
    private Long spacecraftTypeId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Mass capacity must be positive")
    @Digits(integer = 13, fraction = 2, message = "Mass capacity format invalid")
    private BigDecimal massCapacity;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Volume capacity must be positive")
    @Digits(integer = 13, fraction = 2, message = "Volume capacity format invalid")
    private BigDecimal volumeCapacity;

    @NotNull
    private SpacecraftStatus status;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String currentLocation;
}