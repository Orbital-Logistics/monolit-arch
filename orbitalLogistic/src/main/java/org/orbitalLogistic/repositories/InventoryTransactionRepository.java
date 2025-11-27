package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.InventoryTransaction;
import org.orbitalLogistic.entities.enums.TransactionType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends CrudRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByTransactionType(TransactionType transactionType);
    List<InventoryTransaction> findByCargoId(Long cargoId);
    List<InventoryTransaction> findByPerformedByUserId(Long performedByUserId);

    List<InventoryTransaction> findByFromStorageUnitId(Long fromStorageUnitId);
    List<InventoryTransaction> findByToStorageUnitId(Long toStorageUnitId);
    List<InventoryTransaction> findByFromSpacecraftId(Long fromSpacecraftId);
    List<InventoryTransaction> findByToSpacecraftId(Long toSpacecraftId);

    @Query("""
        SELECT it.* FROM inventory_transaction it
        WHERE it.cargo_id = :cargoId
        ORDER BY it.transaction_date DESC
    """)
    List<InventoryTransaction> findByCargoIdOrderByTransactionDate(@Param("cargoId") Long cargoId);

    @Query("""
        SELECT it.* FROM inventory_transaction it
        WHERE (CAST(:transactionType AS TEXT) IS NULL OR it.transaction_type = CAST(:transactionType AS transaction_type_enum))
        AND (CAST(:cargoId AS BIGINT) IS NULL OR it.cargo_id = CAST(:cargoId AS BIGINT))
        AND (CAST(:performedByUserId AS BIGINT) IS NULL OR it.performed_by_user_id = CAST(:performedByUserId AS BIGINT))
        AND (CAST(:fromStorageUnitId AS BIGINT) IS NULL OR it.from_storage_unit_id = CAST(:fromStorageUnitId AS BIGINT))
        AND (CAST(:toStorageUnitId AS BIGINT) IS NULL OR it.to_storage_unit_id = CAST(:toStorageUnitId AS BIGINT))
        AND (CAST(:fromSpacecraftId AS BIGINT) IS NULL OR it.from_spacecraft_id = CAST(:fromSpacecraftId AS BIGINT))
        AND (CAST(:toSpacecraftId AS BIGINT) IS NULL OR it.to_spacecraft_id = CAST(:toSpacecraftId AS BIGINT))
        AND (CAST(:reasonCode AS TEXT) IS NULL OR LOWER(it.reason_code) LIKE LOWER(CONCAT('%', CAST(:reasonCode AS TEXT), '%')))
        ORDER BY it.transaction_date DESC
        LIMIT :limit OFFSET :offset
    """)
    List<InventoryTransaction> findWithFilters(
        @Param("transactionType") String transactionType,
        @Param("cargoId") Long cargoId,
        @Param("performedByUserId") Long performedByUserId,
        @Param("fromStorageUnitId") Long fromStorageUnitId,
        @Param("toStorageUnitId") Long toStorageUnitId,
        @Param("fromSpacecraftId") Long fromSpacecraftId,
        @Param("toSpacecraftId") Long toSpacecraftId,
        @Param("reasonCode") String reasonCode,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM inventory_transaction it
        WHERE (CAST(:transactionType AS TEXT) IS NULL OR it.transaction_type = CAST(:transactionType AS transaction_type_enum))
        AND (CAST(:cargoId AS BIGINT) IS NULL OR it.cargo_id = CAST(:cargoId AS BIGINT))
        AND (CAST(:performedByUserId AS BIGINT) IS NULL OR it.performed_by_user_id = CAST(:performedByUserId AS BIGINT))
        AND (CAST(:fromStorageUnitId AS BIGINT) IS NULL OR it.from_storage_unit_id = CAST(:fromStorageUnitId AS BIGINT))
        AND (CAST(:toStorageUnitId AS BIGINT) IS NULL OR it.to_storage_unit_id = CAST(:toStorageUnitId AS BIGINT))
        AND (CAST(:fromSpacecraftId AS BIGINT) IS NULL OR it.from_spacecraft_id = CAST(:fromSpacecraftId AS BIGINT))
        AND (CAST(:toSpacecraftId AS BIGINT) IS NULL OR it.to_spacecraft_id = CAST(:toSpacecraftId AS BIGINT))
        AND (CAST(:reasonCode AS TEXT) IS NULL OR LOWER(it.reason_code) LIKE LOWER(CONCAT('%', CAST(:reasonCode AS TEXT), '%')))
    """)
    long countWithFilters(
        @Param("transactionType") String transactionType,
        @Param("cargoId") Long cargoId,
        @Param("performedByUserId") Long performedByUserId,
        @Param("fromStorageUnitId") Long fromStorageUnitId,
        @Param("toStorageUnitId") Long toStorageUnitId,
        @Param("fromSpacecraftId") Long fromSpacecraftId,
        @Param("toSpacecraftId") Long toSpacecraftId,
        @Param("reasonCode") String reasonCode
    );

    @Query("""
        SELECT it.* FROM inventory_transaction it
        WHERE it.transaction_date BETWEEN :startDate AND :endDate
        ORDER BY it.transaction_date DESC
    """)
    List<InventoryTransaction> findByTransactionDateBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT it.* FROM inventory_transaction it
        WHERE (it.from_storage_unit_id = :storageUnitId OR it.to_storage_unit_id = :storageUnitId)
        ORDER BY it.transaction_date DESC
    """)
    List<InventoryTransaction> findByStorageUnitId(@Param("storageUnitId") Long storageUnitId);

    @Query("""
        SELECT it.* FROM inventory_transaction it
        WHERE (it.from_spacecraft_id = :spacecraftId OR it.to_spacecraft_id = :spacecraftId)
        ORDER BY it.transaction_date DESC
    """)
    List<InventoryTransaction> findBySpacecraftId(@Param("spacecraftId") Long spacecraftId);

    @Query("""
        SELECT SUM(CASE WHEN it.transaction_type = 'LOAD' THEN it.quantity ELSE -it.quantity END)
        FROM inventory_transaction it
        WHERE it.cargo_id = :cargoId
        AND (it.to_storage_unit_id = :storageUnitId OR it.from_storage_unit_id = :storageUnitId)
    """)
    Integer getNetQuantityForCargoInStorage(
        @Param("cargoId") Long cargoId,
        @Param("storageUnitId") Long storageUnitId
    );
}
