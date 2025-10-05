package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.MissionRequestDTO;
import org.orbitalLogistic.dto.response.MissionResponseDTO;
import org.orbitalLogistic.entities.Mission;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MissionMapper {

    // Entity -> Response DTO
    @Mapping(target = "commandingOfficerName", source = "commandingOfficerName")
    @Mapping(target = "spacecraftName", source = "spacecraftName")
    MissionResponseDTO toResponseDTO(
            Mission mission,
            String commandingOfficerName,
            String spacecraftName
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(org.orbitalLogistic.entities.enums.MissionStatus.PLANNING)")
    @Mapping(target = "priority", expression = "java(org.orbitalLogistic.entities.enums.MissionPriority.MEDIUM)")
    Mission toEntity(MissionRequestDTO request);
}
