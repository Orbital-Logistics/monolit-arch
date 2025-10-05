package org.orbitalLogistic.dto.response;

import org.orbitalLogistic.entities.enums.TransactionType;

import java.time.LocalDateTime;

public record InventoryTransactionResponseDTO(
    Long id,
    TransactionType transactionType,
    String cargoName,
    Integer quantity,
    String fromLocation,
    String toLocation,
    String performedByUserName,
    LocalDateTime transactionDate,
    String reasonCode,
    String referenceNumber,
    String notes
) {}
