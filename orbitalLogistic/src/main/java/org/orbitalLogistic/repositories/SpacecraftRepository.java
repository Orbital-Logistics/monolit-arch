package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpacecraftRepository extends CrudRepository<Spacecraft, Long> {

    Optional<Spacecraft> findByRegistryCode(String registryCode);
    boolean existsByRegistryCode(String registryCode);
    List<Spacecraft> findByStatus(SpacecraftStatus status);

    @Query("""
        SELECT s.* FROM spacecraft s 
        WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) 
        AND (:status IS NULL OR s.status = CAST(:status AS spacecraft_status_enum))
        ORDER BY s.id
        LIMIT :limit OFFSET :offset
    """)
    List<Spacecraft> findWithFilters(
        @Param("name") String name,
        @Param("status") String status,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM spacecraft s 
        WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) 
        AND (:status IS NULL OR s.status = CAST(:status AS spacecraft_status_enum))
    """)
    long countWithFilters(@Param("name") String name, @Param("status") String status);

    @Query("SELECT s.* FROM spacecraft s WHERE s.status IN ('DOCKED', 'MAINTENANCE')")
    List<Spacecraft> findAvailableForMission();
}