package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.CargoStorage;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CargoStorageRepository extends CrudRepository<CargoStorage, Long> {

    List<CargoStorage> findByStorageUnitId(Long storageUnitId);
    List<CargoStorage> findByCargoId(Long cargoId);
    List<CargoStorage> findByStorageUnitIdAndCargoId(Long storageUnitId, Long cargoId);

    List<CargoStorage> findByLastCheckedByUserId(Long userId);

    long countByStorageUnitId(Long storageUnitId);

    @Query("""
        SELECT cs.* FROM cargo_storage cs
        WHERE cs.storage_unit_id = :storageUnitId
        ORDER BY cs.stored_at DESC
    """)
    List<CargoStorage> findByStorageUnitIdOrderByStoredAt(@Param("storageUnitId") Long storageUnitId);

    @Query("""
        SELECT cs.*, c.name as cargo_name, su.unit_code, su.location
        FROM cargo_storage cs
        INNER JOIN cargo c ON cs.cargo_id = c.id
        INNER JOIN storage_unit su ON cs.storage_unit_id = su.id
        WHERE (:storageUnitId IS NULL OR cs.storage_unit_id = :storageUnitId)
        AND (:cargoId IS NULL OR cs.cargo_id = :cargoId)
        AND (:location IS NULL OR LOWER(su.location) LIKE LOWER(CONCAT('%', :location, '%')))
        ORDER BY su.location, c.name
        LIMIT :limit OFFSET :offset
    """)
    List<CargoStorage> findWithFilters(
        @Param("storageUnitId") Long storageUnitId,
        @Param("cargoId") Long cargoId,
        @Param("location") String location,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM cargo_storage cs
        INNER JOIN cargo c ON cs.cargo_id = c.id
        INNER JOIN storage_unit su ON cs.storage_unit_id = su.id
        WHERE (:storageUnitId IS NULL OR cs.storage_unit_id = :storageUnitId)
        AND (:cargoId IS NULL OR cs.cargo_id = :cargoId)
        AND (:location IS NULL OR LOWER(su.location) LIKE LOWER(CONCAT('%', :location, '%')))
    """)
    long countWithFilters(
        @Param("storageUnitId") Long storageUnitId,
        @Param("cargoId") Long cargoId,
        @Param("location") String location
    );

    @Query("""
        SELECT cs.* FROM cargo_storage cs
        WHERE cs.last_inventory_check IS NULL 
        OR cs.last_inventory_check < :beforeDate
        ORDER BY cs.last_inventory_check NULLS FIRST
    """)
    List<CargoStorage> findRequiringInventoryCheck(@Param("beforeDate") LocalDateTime beforeDate);

    @Query("""
        SELECT SUM(cs.quantity) FROM cargo_storage cs
        WHERE cs.cargo_id = :cargoId
    """)
    Integer getTotalQuantityByCargoId(@Param("cargoId") Long cargoId);

    @Query("""
        SELECT SUM(cs.quantity) FROM cargo_storage cs
        WHERE cs.storage_unit_id = :storageUnitId
    """)
    Integer getTotalQuantityByStorageUnitId(@Param("storageUnitId") Long storageUnitId);
}
