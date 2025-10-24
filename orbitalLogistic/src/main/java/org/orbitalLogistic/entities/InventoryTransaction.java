package org.orbitalLogistic.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.orbitalLogistic.entities.enums.TransactionType;

import java.time.LocalDateTime;

@Table("inventory_transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {

    @Id
    private Long id;

    @NotNull
    @Column("transaction_type")
    private TransactionType transactionType;

    @NotNull(message = "Cargo is required")
    @Column("cargo_id")
    private Long cargoId;

    @NotNull
    @Column("quantity")
    private Integer quantity;

    @Column("from_storage_unit_id")
    private Long fromStorageUnitId;

    @Column("to_storage_unit_id")
    private Long toStorageUnitId;

    @Column("from_spacecraft_id")
    private Long fromSpacecraftId;

    @Column("to_spacecraft_id")
    private Long toSpacecraftId;

    @NotNull(message = "Performed by user is required")
    @Column("performed_by_user_id")
    private Long performedByUserId;

    @Builder.Default
    @Column("transaction_date")
    private LocalDateTime transactionDate = LocalDateTime.now();

    @Size(max = 50, message = "Reason code must not exceed 50 characters")
    @Column("reason_code")
    private String reasonCode;

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    @Column("reference_number")
    private String referenceNumber;

    @Column("notes")
    private String notes;
}
