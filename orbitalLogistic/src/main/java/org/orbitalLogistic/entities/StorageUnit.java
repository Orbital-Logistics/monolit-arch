package org.orbitalLogistic.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.orbitalLogistic.entities.enums.StorageType;

import java.math.BigDecimal;

@Table("storage_unit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageUnit {

    @Id
    private Long id;

    @NotBlank
    @Size(max = 20, message = "Unit code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Unit code can only contain uppercase letters, numbers and hyphens")
    private String unitCode;

    @NotBlank
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @NotNull
    private StorageType storageType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Total mass capacity must be positive")
    @Digits(integer = 13, fraction = 2, message = "Total mass capacity format invalid")
    private BigDecimal totalMassCapacity;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Total volume capacity must be positive")
    @Digits(integer = 13, fraction = 2, message = "Total volume capacity format invalid")
    private BigDecimal totalVolumeCapacity;

    @NotNull
    @DecimalMin(value = "0.0", message = "Current mass cannot be negative")
    private BigDecimal currentMass;

    @NotNull
    @DecimalMin(value = "0.0", message = "Current volume cannot be negative")
    private BigDecimal currentVolume;
}