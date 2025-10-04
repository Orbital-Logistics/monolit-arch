package org.orbitalLogistic.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.orbitalLogistic.entities.enums.CargoType;
import org.orbitalLogistic.entities.enums.HazardLevel;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("cargo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cargo {

    @Id
    private Long id;

    @NotBlank
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Cargo category is required")
    @Column("cargo_category_id")
    private Long cargoCategoryId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Mass per unit must be positive")
    @Digits(integer = 8, fraction = 2, message = "Mass per unit format invalid")
    @Column("mass_per_unit")
    private BigDecimal massPerUnit;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Volume per unit must be positive")
    @Digits(integer = 8, fraction = 2, message = "Volume per unit format invalid")
    @Column("volume_per_unit")
    private BigDecimal volumePerUnit;

    @NotNull
    @Column("cargo_type")
    private CargoType cargoType;

    @NotNull
    @Column("hazard_level")
    private HazardLevel hazardLevel;

    @Column("is_active")
    private Boolean isActive = true;
}
