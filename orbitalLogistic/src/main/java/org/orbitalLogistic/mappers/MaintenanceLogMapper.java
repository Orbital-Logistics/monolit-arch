package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.dto.response.MaintenanceLogResponseDTO;
import org.orbitalLogistic.entities.MaintenanceLog;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MaintenanceLogMapper {

    // Entity -> Response DTO
    @Mapping(target = "spacecraftName", source = "spacecraftName")
    @Mapping(target = "performedByUserName", source = "performedByUserName")
    @Mapping(target = "supervisedByUserName", source = "supervisedByUserName")
    MaintenanceLogResponseDTO toResponseDTO(
            MaintenanceLog maintenanceLog,
            String spacecraftName,
            String performedByUserName,
            String supervisedByUserName
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(org.orbitalLogistic.entities.enums.MaintenanceStatus.SCHEDULED)")
    MaintenanceLog toEntity(MaintenanceLogRequestDTO request);
}
