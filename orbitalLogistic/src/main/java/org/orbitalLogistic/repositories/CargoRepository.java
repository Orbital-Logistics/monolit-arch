package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.Cargo;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CargoRepository extends CrudRepository<Cargo, Long> {

    Optional<Cargo> findByName(String name);
    boolean existsByName(String name);
    List<Cargo> findByCargoCategoryId(Long categoryId);

    @Query("""
        SELECT c.* FROM cargo c 
        WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) 
        AND (:cargoType IS NULL OR c.cargo_type = :cargoType)
        AND (:hazardLevel IS NULL OR c.hazard_level = :hazardLevel)
        ORDER BY c.id
        LIMIT :limit OFFSET :offset
    """)
    List<Cargo> findWithFilters(
        @Param("name") String name,
        @Param("cargoType") String cargoType,
        @Param("hazardLevel") String hazardLevel,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM cargo c 
        WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) 
        AND (:cargoType IS NULL OR c.cargo_type = :cargoType)
        AND (:hazardLevel IS NULL OR c.hazard_level = :hazardLevel)
    """)
    long countWithFilters(
        @Param("name") String name,
        @Param("cargoType") String cargoType,
        @Param("hazardLevel") String hazardLevel
    );
}