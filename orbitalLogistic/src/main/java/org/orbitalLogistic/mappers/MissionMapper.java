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

    // Request DTO -> Entity (для создания новой миссии)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(org.orbitalLogistic.entities.enums.MissionStatus.PLANNING)")
    @Mapping(target = "actualArrival", ignore = true)
    @Mapping(target = "completedByUserId", ignore = true)
    @Mapping(target = "completionNotes", ignore = true)
    @Mapping(target = "isSuccessful", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "cargoItems", ignore = true)
    Mission toEntity(MissionRequestDTO request);
}
