package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftTypeResponseDTO;
import org.orbitalLogistic.entities.SpacecraftType;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpacecraftTypeMapper {

    // Entity -> Response DTO
    SpacecraftTypeResponseDTO toResponseDTO(SpacecraftType spacecraftType);

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    SpacecraftType toEntity(SpacecraftTypeRequestDTO request);
}
