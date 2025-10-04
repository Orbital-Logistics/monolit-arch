package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.StorageUnit;
import org.orbitalLogistic.entities.enums.StorageType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageUnitRepository extends CrudRepository<StorageUnit, Long> {

    Optional<StorageUnit> findByUnitCode(String unitCode);
    boolean existsByUnitCode(String unitCode);
    List<StorageUnit> findByStorageType(StorageType storageType);

    @Query("SELECT su.* FROM storage_unit su ORDER BY su.id LIMIT :limit OFFSET :offset")
    List<StorageUnit> findAllPaged(@Param("limit") int limit, @Param("offset") int offset);

    @Query("SELECT COUNT(*) FROM storage_unit su")
    long countAll();

    @Query("""
        SELECT su.* FROM storage_unit su 
        WHERE su.total_mass_capacity - su.current_mass >= :requiredMass 
        AND su.total_volume_capacity - su.current_volume >= :requiredVolume
    """)
    List<StorageUnit> findSuitableForCargo(
        @Param("requiredMass") Double requiredMass,
        @Param("requiredVolume") Double requiredVolume
    );
}
