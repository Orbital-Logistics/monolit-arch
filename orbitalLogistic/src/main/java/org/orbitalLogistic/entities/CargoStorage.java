package org.orbitalLogistic.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("cargo_storage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoStorage {

    @Id
    private Long id;

    @NotNull(message = "Storage unit is required")
    @Column("storage_unit_id")
    private Long storageUnitId;

    @NotNull(message = "Cargo is required")
    @Column("cargo_id")
    private Long cargoId;

    @NotNull
    @Min(value = 0, message = "Quantity must be non-negative")
    @Column("quantity")
    private Integer quantity;

    @Builder.Default
    @Column("stored_at")
    private LocalDateTime storedAt = LocalDateTime.now();

    @Column("last_inventory_check")
    private LocalDateTime lastInventoryCheck;

    @Column("last_checked_by_user_id")
    private Long lastCheckedByUserId;
}
