package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.Mission;
import org.orbitalLogistic.entities.enums.MissionStatus;
import org.orbitalLogistic.entities.enums.MissionType;
import org.orbitalLogistic.entities.enums.MissionPriority;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MissionRepository extends CrudRepository<Mission, Long> {

    Optional<Mission> findByMissionCode(String missionCode);
    boolean existsByMissionCode(String missionCode);

    List<Mission> findByStatus(MissionStatus status);
    List<Mission> findByMissionType(MissionType missionType);
    List<Mission> findByPriority(MissionPriority priority);

    List<Mission> findByCommandingOfficerId(Long commandingOfficerId);
    List<Mission> findBySpacecraftId(Long spacecraftId);

    @Query("""
        SELECT m.* FROM mission m 
        WHERE (CAST(:missionCode AS TEXT) IS NULL OR LOWER(m.mission_code) LIKE LOWER(CONCAT('%', CAST(:missionCode AS TEXT), '%')))
        AND (CAST(:missionName AS TEXT) IS NULL OR LOWER(m.mission_name) LIKE LOWER(CONCAT('%', CAST(:missionName AS TEXT), '%')))
        AND (CAST(:status AS TEXT) IS NULL OR m.status = CAST(:status AS mission_status_enum))
        AND (CAST(:missionType AS TEXT) IS NULL OR m.mission_type = CAST(:missionType AS mission_type_enum))
        AND (CAST(:priority AS TEXT) IS NULL OR m.priority = CAST(:priority AS mission_priority_enum))
        AND (CAST(:commandingOfficerId AS BIGINT) IS NULL OR m.commanding_officer_id = CAST(:commandingOfficerId AS BIGINT))
        AND (CAST(:spacecraftId AS BIGINT) IS NULL OR m.spacecraft_id = CAST(:spacecraftId AS BIGINT))
        ORDER BY m.scheduled_departure DESC NULLS LAST, m.mission_code
        LIMIT :limit OFFSET :offset
    """)
    List<Mission> findWithFilters(
        @Param("missionCode") String missionCode,
        @Param("missionName") String missionName,
        @Param("status") String status,
        @Param("missionType") String missionType,
        @Param("priority") String priority,
        @Param("commandingOfficerId") Long commandingOfficerId,
        @Param("spacecraftId") Long spacecraftId,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM mission m 
        WHERE (CAST(:missionCode AS TEXT) IS NULL OR LOWER(m.mission_code) LIKE LOWER(CONCAT('%', CAST(:missionCode AS TEXT), '%')))
        AND (CAST(:missionName AS TEXT) IS NULL OR LOWER(m.mission_name) LIKE LOWER(CONCAT('%', CAST(:missionName AS TEXT), '%')))
        AND (CAST(:status AS TEXT) IS NULL OR m.status = CAST(:status AS mission_status_enum))
        AND (CAST(:missionType AS TEXT) IS NULL OR m.mission_type = CAST(:missionType AS mission_type_enum))
        AND (CAST(:priority AS TEXT) IS NULL OR m.priority = CAST(:priority AS mission_priority_enum))
        AND (CAST(:commandingOfficerId AS BIGINT) IS NULL OR m.commanding_officer_id = CAST(:commandingOfficerId AS BIGINT))
        AND (CAST(:spacecraftId AS BIGINT) IS NULL OR m.spacecraft_id = CAST(:spacecraftId AS BIGINT))
    """)
    long countWithFilters(
        @Param("missionCode") String missionCode,
        @Param("missionName") String missionName,
        @Param("status") String status,
        @Param("missionType") String missionType,
        @Param("priority") String priority,
        @Param("commandingOfficerId") Long commandingOfficerId,
        @Param("spacecraftId") Long spacecraftId
    );

    @Query("""
        SELECT m.* FROM mission m 
        WHERE m.scheduled_departure BETWEEN :startDate AND :endDate
        ORDER BY m.scheduled_departure
    """)
    List<Mission> findByScheduledDepartureBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT m.* FROM mission m 
        WHERE m.status IN ('SCHEDULED', 'IN_PROGRESS')
        AND m.scheduled_departure <= :dateTime
        ORDER BY m.priority DESC, m.scheduled_departure
    """)
    List<Mission> findActiveMissions(@Param("dateTime") LocalDateTime dateTime);
}
