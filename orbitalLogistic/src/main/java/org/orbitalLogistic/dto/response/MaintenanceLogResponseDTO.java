package org.orbitalLogistic.dto.response;

import org.orbitalLogistic.entities.enums.MaintenanceType;
import org.orbitalLogistic.entities.enums.MaintenanceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaintenanceLogResponseDTO(
    Long id,
    String spacecraftName,
    MaintenanceType maintenanceType,
    MaintenanceStatus status,
    String performedByUserName,
    String supervisedByUserName,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String description,
    BigDecimal cost
) {}
