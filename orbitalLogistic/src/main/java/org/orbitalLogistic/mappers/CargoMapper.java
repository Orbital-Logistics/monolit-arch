package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.CargoRequestDTO;
import org.orbitalLogistic.dto.response.CargoResponseDTO;
import org.orbitalLogistic.entities.Cargo;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CargoMapper {

    // Entity -> Response DTO
    @Mapping(target = "cargoCategoryName", source = "cargoCategoryName")
    @Mapping(target = "totalQuantity", source = "totalQuantity")
    CargoResponseDTO toResponseDTO(
            Cargo cargo,
            String cargoCategoryName,
            Integer totalQuantity
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    Cargo toEntity(CargoRequestDTO request);
}