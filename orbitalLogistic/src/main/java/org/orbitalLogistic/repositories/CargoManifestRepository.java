package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.CargoManifest;
import org.orbitalLogistic.entities.enums.ManifestStatus;
import org.orbitalLogistic.entities.enums.ManifestPriority;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CargoManifestRepository extends CrudRepository<CargoManifest, Long> {

    List<CargoManifest> findBySpacecraftId(Long spacecraftId);
    List<CargoManifest> findByCargoId(Long cargoId);
    List<CargoManifest> findByStorageUnitId(Long storageUnitId);
    List<CargoManifest> findByManifestStatus(ManifestStatus manifestStatus);
    List<CargoManifest> findByPriority(ManifestPriority priority);

    List<CargoManifest> findByLoadedByUserId(Long loadedByUserId);
    List<CargoManifest> findByUnloadedByUserId(Long unloadedByUserId);

    @Query("""
        SELECT cm.* FROM cargo_manifest cm
        WHERE cm.spacecraft_id = :spacecraftId
        ORDER BY cm.priority DESC, cm.loaded_at DESC NULLS LAST
    """)
    List<CargoManifest> findBySpacecraftIdOrderByPriorityAndLoadedAt(@Param("spacecraftId") Long spacecraftId);

    @Query("""
        SELECT cm.* FROM cargo_manifest cm
        WHERE (:spacecraftId IS NULL OR cm.spacecraft_id = :spacecraftId)
        AND (:cargoId IS NULL OR cm.cargo_id = :cargoId)
        AND (:storageUnitId IS NULL OR cm.storage_unit_id = :storageUnitId)
        AND (:manifestStatus IS NULL OR cm.manifest_status = :manifestStatus)
        AND (:priority IS NULL OR cm.priority = :priority)
        AND (:loadedByUserId IS NULL OR cm.loaded_by_user_id = :loadedByUserId)
        ORDER BY cm.priority DESC, cm.loaded_at DESC NULLS LAST
        LIMIT :limit OFFSET :offset
    """)
    List<CargoManifest> findWithFilters(
        @Param("spacecraftId") Long spacecraftId,
        @Param("cargoId") Long cargoId,
        @Param("storageUnitId") Long storageUnitId,
        @Param("manifestStatus") String manifestStatus,
        @Param("priority") String priority,
        @Param("loadedByUserId") Long loadedByUserId,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM cargo_manifest cm
        WHERE (:spacecraftId IS NULL OR cm.spacecraft_id = :spacecraftId)
        AND (:cargoId IS NULL OR cm.cargo_id = :cargoId)
        AND (:storageUnitId IS NULL OR cm.storage_unit_id = :storageUnitId)
        AND (:manifestStatus IS NULL OR cm.manifest_status = :manifestStatus)
        AND (:priority IS NULL OR cm.priority = :priority)
        AND (:loadedByUserId IS NULL OR cm.loaded_by_user_id = :loadedByUserId)
    """)
    long countWithFilters(
        @Param("spacecraftId") Long spacecraftId,
        @Param("cargoId") Long cargoId,
        @Param("storageUnitId") Long storageUnitId,
        @Param("manifestStatus") String manifestStatus,
        @Param("priority") String priority,
        @Param("loadedByUserId") Long loadedByUserId
    );

    @Query("""
        SELECT cm.* FROM cargo_manifest cm
        WHERE cm.loaded_at BETWEEN :startDate AND :endDate
        ORDER BY cm.loaded_at
    """)
    List<CargoManifest> findByLoadedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT cm.* FROM cargo_manifest cm
        WHERE cm.manifest_status IN ('LOADED', 'IN_TRANSIT')
        AND cm.spacecraft_id = :spacecraftId
        ORDER BY cm.priority DESC
    """)
    List<CargoManifest> findActiveCargoBySpacecraft(@Param("spacecraftId") Long spacecraftId);

    @Query("""
        SELECT SUM(cm.quantity) FROM cargo_manifest cm
        WHERE cm.spacecraft_id = :spacecraftId
        AND cm.cargo_id = :cargoId
        AND cm.manifest_status IN ('LOADED', 'IN_TRANSIT')
    """)
    Integer getTotalQuantityOnSpacecraft(
        @Param("spacecraftId") Long spacecraftId,
        @Param("cargoId") Long cargoId
    );

    @Query("""
        SELECT cm.* FROM cargo_manifest cm
        WHERE cm.manifest_status = 'PENDING'
        AND cm.priority = 'CRITICAL'
        ORDER BY cm.loaded_at NULLS FIRST
    """)
    List<CargoManifest> findCriticalPendingManifests();
}
