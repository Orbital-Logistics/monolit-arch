package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftResponseDTO;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.enums.SpacecraftClassification;

import java.math.BigDecimal;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpacecraftMapper {

    // Entity -> Response DTO
    @Mapping(target = "spacecraftTypeName", source = "spacecraftTypeName")
    @Mapping(target = "classification", source = "classification")
    @Mapping(target = "currentMassUsage", source = "currentMassUsage")
    @Mapping(target = "currentVolumeUsage", source = "currentVolumeUsage")
    SpacecraftResponseDTO toResponseDTO(
            Spacecraft spacecraft,
            String spacecraftTypeName,
            SpacecraftClassification classification,
            BigDecimal currentMassUsage,
            BigDecimal currentVolumeUsage
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(org.orbitalLogistic.entities.enums.SpacecraftStatus.DOCKED)")
    @Mapping(target = "currentLocation", defaultExpression = "java(\"Orbital Station Alpha\")")
    Spacecraft toEntity(SpacecraftRequestDTO request);
}
