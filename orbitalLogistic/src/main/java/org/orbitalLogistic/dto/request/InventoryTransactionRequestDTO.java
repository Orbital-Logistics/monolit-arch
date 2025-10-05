package org.orbitalLogistic.dto.request;

import jakarta.validation.constraints.*;
import org.orbitalLogistic.entities.enums.TransactionType;

public record InventoryTransactionRequestDTO(
    @NotNull(message = "Transaction type is required")
    TransactionType transactionType,

    @NotNull(message = "Cargo ID is required")
    Long cargoId,

    @NotNull(message = "Quantity is required")
    Integer quantity,

    Long fromStorageUnitId,
    Long toStorageUnitId,
    Long fromSpacecraftId,
    Long toSpacecraftId,

    @NotNull(message = "Performed by user ID is required")
    Long performedByUserId,

    @Size(max = 50, message = "Reason code must not exceed 50 characters")
    String reasonCode,

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    String referenceNumber,

    String notes
) {}
