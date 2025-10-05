package org.orbitalLogistic.entities;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.orbitalLogistic.entities.enums.MissionType;
import org.orbitalLogistic.entities.enums.MissionStatus;
import org.orbitalLogistic.entities.enums.MissionPriority;

import java.time.LocalDateTime;

@Table("mission")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mission {

    @Id
    private Long id;

    @NotBlank
    @Size(max = 20, message = "Mission code must not exceed 20 characters")
    @Column("mission_code")
    private String missionCode;

    @NotBlank
    @Size(max = 200, message = "Mission name must not exceed 200 characters")
    @Column("mission_name")
    private String missionName;

    @NotNull
    @Column("mission_type")
    private MissionType missionType;

    @NotNull
    @Builder.Default
    @Column("status")
    private MissionStatus status = MissionStatus.PLANNING;

    @NotNull
    @Builder.Default
    @Column("priority")
    private MissionPriority priority = MissionPriority.MEDIUM;

    @NotNull(message = "Commanding officer is required")
    @Column("commanding_officer_id")
    private Long commandingOfficerId;

    @NotNull(message = "Spacecraft is required")
    @Column("spacecraft_id")
    private Long spacecraftId;

    @Column("scheduled_departure")
    private LocalDateTime scheduledDeparture;

    @Column("scheduled_arrival")
    private LocalDateTime scheduledArrival;
}
