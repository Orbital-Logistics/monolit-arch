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
        WHERE (:transactionType IS NULL OR it.transaction_type = CAST(:transactionType AS transaction_type_enum))
        AND (:cargoId IS NULL OR it.cargo_id = :cargoId)
        AND (:performedByUserId IS NULL OR it.performed_by_user_id = :performedByUserId)
        AND (:fromStorageUnitId IS NULL OR it.from_storage_unit_id = :fromStorageUnitId)
        AND (:toStorageUnitId IS NULL OR it.to_storage_unit_id = :toStorageUnitId)
        AND (:fromSpacecraftId IS NULL OR it.from_spacecraft_id = :fromSpacecraftId)
        AND (:toSpacecraftId IS NULL OR it.to_spacecraft_id = :toSpacecraftId)
        AND (:reasonCode IS NULL OR LOWER(it.reason_code) LIKE LOWER(CONCAT('%', :reasonCode, '%')))
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
        WHERE (:transactionType IS NULL OR it.transaction_type = CAST(:transactionType AS transaction_type_enum))
        AND (:cargoId IS NULL OR it.cargo_id = :cargoId)
        AND (:performedByUserId IS NULL OR it.performed_by_user_id = :performedByUserId)
        AND (:fromStorageUnitId IS NULL OR it.from_storage_unit_id = :fromStorageUnitId)
        AND (:toStorageUnitId IS NULL OR it.to_storage_unit_id = :toStorageUnitId)
        AND (:fromSpacecraftId IS NULL OR it.from_spacecraft_id = :fromSpacecraftId)
        AND (:toSpacecraftId IS NULL OR it.to_spacecraft_id = :toSpacecraftId)
        AND (:reasonCode IS NULL OR LOWER(it.reason_code) LIKE LOWER(CONCAT('%', :reasonCode, '%')))
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
