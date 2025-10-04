package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.dto.response.StorageUnitResponseDTO;
import org.orbitalLogistic.entities.StorageUnit;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StorageUnitMapper {

    // Entity -> Response DTO
    @Mapping(target = "availableMassCapacity", source = "availableMassCapacity")
    @Mapping(target = "availableVolumeCapacity", source = "availableVolumeCapacity")
    @Mapping(target = "massUsagePercentage", source = "massUsagePercentage")
    @Mapping(target = "volumeUsagePercentage", source = "volumeUsagePercentage")
    StorageUnitResponseDTO toResponseDTO(
            StorageUnit storageUnit,
            BigDecimal availableMassCapacity,
            BigDecimal availableVolumeCapacity,
            Double massUsagePercentage,
            Double volumeUsagePercentage
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentMass", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "currentVolume", expression = "java(java.math.BigDecimal.ZERO)")
    StorageUnit toEntity(StorageUnitRequestDTO request);
}
