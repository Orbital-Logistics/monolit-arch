package org.orbitalLogistic.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.orbitalLogistic.entities.enums.ManifestPriority;
import org.orbitalLogistic.entities.enums.ManifestStatus;

import java.time.LocalDateTime;

@Table("cargo_manifest")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoManifest {

    @Id
    private Long id;

    @NotNull(message = "Spacecraft is required")
    @Column("spacecraft_id")
    private Long spacecraftId;

    @NotNull(message = "Cargo is required")
    @Column("cargo_id")
    private Long cargoId;

    @NotNull(message = "Storage unit is required")
    @Column("storage_unit_id")
    private Long storageUnitId;

    @NotNull
    @Min(value = 1, message = "Quantity must be positive")
    @Column("quantity")
    private Integer quantity;

    @Column("loaded_at")
    private LocalDateTime loadedAt;

    @Column("unloaded_at")
    private LocalDateTime unloadedAt;

    @NotNull(message = "Loaded by user is required")
    @Column("loaded_by_user_id")
    private Long loadedByUserId;

    @Column("unloaded_by_user_id")
    private Long unloadedByUserId;

    @NotNull
    @Builder.Default
    @Column("manifest_status")
    private ManifestStatus manifestStatus = ManifestStatus.PENDING;

    @NotNull
    @Builder.Default
    @Column("priority")
    private ManifestPriority priority = ManifestPriority.NORMAL;
}
