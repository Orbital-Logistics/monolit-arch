package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.CargoManifestRequestDTO;
import org.orbitalLogistic.dto.response.CargoManifestResponseDTO;
import org.orbitalLogistic.entities.CargoManifest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CargoManifestMapper {

    // Entity -> Response DTO
    @Mapping(target = "spacecraftName", source = "spacecraftName")
    @Mapping(target = "cargoName", source = "cargoName")
    @Mapping(target = "storageUnitCode", source = "storageUnitCode")
    @Mapping(target = "loadedByUserName", source = "loadedByUserName")
    @Mapping(target = "unloadedByUserName", source = "unloadedByUserName")
    CargoManifestResponseDTO toResponseDTO(
            CargoManifest cargoManifest,
            String spacecraftName,
            String cargoName,
            String storageUnitCode,
            String loadedByUserName,
            String unloadedByUserName
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manifestStatus", expression = "java(org.orbitalLogistic.entities.enums.ManifestStatus.PENDING)")
    // Игнорируем поля для массовых операций
    @Mapping(target = "cargoItems", ignore = true)
    @Mapping(target = "targetStorageUnitId", ignore = true)
    @Mapping(target = "unloadedByUserId", ignore = true)
    @Mapping(target = "operation", ignore = true)
    @Mapping(target = "notes", ignore = true)
    CargoManifest toEntity(CargoManifestRequestDTO request);
}
