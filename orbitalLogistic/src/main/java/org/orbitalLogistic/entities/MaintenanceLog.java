package org.orbitalLogistic.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.orbitalLogistic.entities.enums.MaintenanceType;
import org.orbitalLogistic.entities.enums.MaintenanceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("maintenance_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceLog {

    @Id
    private Long id;

    @NotNull(message = "Spacecraft is required")
    @Column("spacecraft_id")
    private Long spacecraftId;

    @NotNull
    @Column("maintenance_type")
    private MaintenanceType maintenanceType;

    @NotNull(message = "Performed by user is required")
    @Column("performed_by_user_id")
    private Long performedByUserId;

    @Column("supervised_by_user_id")
    private Long supervisedByUserId;

    @Column("start_time")
    private LocalDateTime startTime;

    @Column("end_time")
    private LocalDateTime endTime;

    @NotNull
    @Builder.Default
    @Column("status")
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    @Column("description")
    private String description;

    @DecimalMin(value = "0.0", message = "Cost must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Cost format invalid")
    @Column("cost")
    private BigDecimal cost;
}
