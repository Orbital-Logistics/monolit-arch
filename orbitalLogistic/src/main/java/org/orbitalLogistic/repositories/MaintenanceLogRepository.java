package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.MaintenanceLog;
import org.orbitalLogistic.entities.enums.MaintenanceType;
import org.orbitalLogistic.entities.enums.MaintenanceStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceLogRepository extends CrudRepository<MaintenanceLog, Long> {

    List<MaintenanceLog> findBySpacecraftId(Long spacecraftId);
    List<MaintenanceLog> findByMaintenanceType(MaintenanceType maintenanceType);
    List<MaintenanceLog> findByStatus(MaintenanceStatus status);
    List<MaintenanceLog> findByPerformedByUserId(Long performedByUserId);
    List<MaintenanceLog> findBySupervisedByUserId(Long supervisedByUserId);

    @Query("""
        SELECT ml.* FROM maintenance_log ml
        WHERE ml.spacecraft_id = :spacecraftId
        ORDER BY ml.start_time DESC NULLS LAST
    """)
    List<MaintenanceLog> findBySpacecraftIdOrderByStartTime(@Param("spacecraftId") Long spacecraftId);

    @Query("""
        SELECT ml.* FROM maintenance_log ml
        WHERE (:spacecraftId IS NULL OR ml.spacecraft_id = :spacecraftId)
        AND (:maintenanceType IS NULL OR ml.maintenance_type = :maintenanceType)
        AND (:status IS NULL OR ml.status = :status)
        AND (:performedByUserId IS NULL OR ml.performed_by_user_id = :performedByUserId)
        AND (:supervisedByUserId IS NULL OR ml.supervised_by_user_id = :supervisedByUserId)
        ORDER BY ml.start_time DESC NULLS LAST
        LIMIT :limit OFFSET :offset
    """)
    List<MaintenanceLog> findWithFilters(
        @Param("spacecraftId") Long spacecraftId,
        @Param("maintenanceType") String maintenanceType,
        @Param("status") String status,
        @Param("performedByUserId") Long performedByUserId,
        @Param("supervisedByUserId") Long supervisedByUserId,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM maintenance_log ml
        WHERE (:spacecraftId IS NULL OR ml.spacecraft_id = :spacecraftId)
        AND (:maintenanceType IS NULL OR ml.maintenance_type = :maintenanceType)
        AND (:status IS NULL OR ml.status = :status)
        AND (:performedByUserId IS NULL OR ml.performed_by_user_id = :performedByUserId)
        AND (:supervisedByUserId IS NULL OR ml.supervised_by_user_id = :supervisedByUserId)
    """)
    long countWithFilters(
        @Param("spacecraftId") Long spacecraftId,
        @Param("maintenanceType") String maintenanceType,
        @Param("status") String status,
        @Param("performedByUserId") Long performedByUserId,
        @Param("supervisedByUserId") Long supervisedByUserId
    );

    @Query("""
        SELECT ml.* FROM maintenance_log ml
        WHERE ml.start_time BETWEEN :startDate AND :endDate
        ORDER BY ml.start_time
    """)
    List<MaintenanceLog> findByStartTimeBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT ml.* FROM maintenance_log ml
        WHERE ml.status = 'SCHEDULED'
        AND ml.start_time <= :dateTime
        ORDER BY ml.start_time
    """)
    List<MaintenanceLog> findScheduledMaintenance(@Param("dateTime") LocalDateTime dateTime);

    @Query("""
        SELECT SUM(ml.cost) FROM maintenance_log ml
        WHERE ml.spacecraft_id = :spacecraftId
        AND ml.status = 'COMPLETED'
        AND ml.start_time BETWEEN :startDate AND :endDate
    """)
    BigDecimal getTotalMaintenanceCost(
        @Param("spacecraftId") Long spacecraftId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT ml.* FROM maintenance_log ml
        WHERE ml.spacecraft_id = :spacecraftId
        AND ml.maintenance_type = :maintenanceType
        ORDER BY ml.start_time DESC
        LIMIT 1
    """)
    MaintenanceLog findLastMaintenanceByTypeAndSpacecraft(
        @Param("spacecraftId") Long spacecraftId,
        @Param("maintenanceType") String maintenanceType
    );
}
