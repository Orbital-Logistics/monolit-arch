package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.CargoCategoryRequestDTO;
import org.orbitalLogistic.dto.response.CargoCategoryResponseDTO;
import org.orbitalLogistic.entities.CargoCategory;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CargoCategoryMapper {

    // Entity -> Response DTO
    @Mapping(target = "parentCategoryName", source = "parentCategoryName")
    @Mapping(target = "children", source = "children")
    @Mapping(target = "level", source = "level")
    CargoCategoryResponseDTO toResponseDTO(
            CargoCategory cargoCategory,
            String parentCategoryName,
            List<CargoCategoryResponseDTO> children,
            Integer level
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    CargoCategory toEntity(CargoCategoryRequestDTO request);

    List<CargoCategoryResponseDTO> toResponseDTOList(List<CargoCategory> categories);
}
