package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.CargoStorageRequestDTO;
import org.orbitalLogistic.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.entities.CargoStorage;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CargoStorageMapper {

    // Entity -> Response DTO
    @Mapping(target = "storageUnitCode", source = "storageUnitCode")
    @Mapping(target = "storageLocation", source = "storageLocation")
    @Mapping(target = "cargoName", source = "cargoName")
    @Mapping(target = "lastCheckedByUserName", source = "lastCheckedByUserName")
    CargoStorageResponseDTO toResponseDTO(
            CargoStorage cargoStorage,
            String storageUnitCode,
            String storageLocation,
            String cargoName,
            String lastCheckedByUserName
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "storedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastInventoryCheck", ignore = true)
    @Mapping(target = "lastCheckedByUserId", ignore = true)
    CargoStorage toEntity(CargoStorageRequestDTO request);
}
