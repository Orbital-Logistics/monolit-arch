package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.SpacecraftMissionRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftMissionResponseDTO;
import org.orbitalLogistic.entities.SpacecraftMission;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpacecraftMissionMapper {

    // Entity -> Response DTO
    @Mapping(target = "spacecraftName", source = "spacecraftName")
    @Mapping(target = "registryCode", source = "registryCode")
    @Mapping(target = "missionName", source = "missionName")
    @Mapping(target = "missionCode", source = "missionCode")
    @Mapping(target = "roleDescription", source = "roleDescription")
    @Mapping(target = "assignedByUserName", source = "assignedByUserName")
    SpacecraftMissionResponseDTO toResponseDTO(
            SpacecraftMission spacecraftMission,
            String spacecraftName,
            String registryCode,
            String missionName,
            String missionCode,
            String roleDescription,
            String assignedByUserName
    );

    // Request DTO -> Entity
    SpacecraftMission toEntity(SpacecraftMissionRequestDTO request);
}
