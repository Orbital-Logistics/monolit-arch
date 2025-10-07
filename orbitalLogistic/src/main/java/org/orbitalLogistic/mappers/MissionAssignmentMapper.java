package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.dto.response.MissionAssignmentResponseDTO;
import org.orbitalLogistic.entities.MissionAssignment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MissionAssignmentMapper {

    // Entity -> Response DTO
    @Mapping(target = "missionName", source = "missionName")
    @Mapping(target = "userName", source = "userName")
    MissionAssignmentResponseDTO toResponseDTO(
            MissionAssignment missionAssignment,
            String missionName,
            String userName
    );

    // Request DTO -> Entity (для индивидуального назначения)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedAt", expression = "java(java.time.LocalDateTime.now())")
    MissionAssignment toEntity(MissionAssignmentRequestDTO request);
}
