package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.dto.response.InventoryTransactionResponseDTO;
import org.orbitalLogistic.entities.InventoryTransaction;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryTransactionMapper {

    // Entity -> Response DTO
    @Mapping(target = "cargoName", source = "cargoName")
    @Mapping(target = "fromLocation", source = "fromLocation")
    @Mapping(target = "toLocation", source = "toLocation")
    @Mapping(target = "performedByUserName", source = "performedByUserName")
    InventoryTransactionResponseDTO toResponseDTO(
            InventoryTransaction inventoryTransaction,
            String cargoName,
            String fromLocation,
            String toLocation,
            String performedByUserName
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionDate", expression = "java(java.time.LocalDateTime.now())")
    InventoryTransaction toEntity(InventoryTransactionRequestDTO request);
}
