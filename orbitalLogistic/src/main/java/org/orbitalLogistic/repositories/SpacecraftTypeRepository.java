package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.entities.SpacecraftType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpacecraftTypeRepository extends CrudRepository<SpacecraftType, Long> {

    Optional<SpacecraftType> findByTypeName(String typeName);
    boolean existsByTypeName(String typeName);
    
    List<SpacecraftType> findByClassification(SpacecraftClassification classification);

    @Query("""
        SELECT st.* FROM spacecraft_type st 
        WHERE (CAST(:typeName AS TEXT) IS NULL OR LOWER(st.type_name) LIKE LOWER(CONCAT('%', CAST(:typeName AS TEXT), '%'))) 
        AND (:classification IS NULL OR st.classification = :classification)
        ORDER BY st.type_name
        LIMIT :limit OFFSET :offset
    """)
    List<SpacecraftType> findWithFilters(
        @Param("typeName") String typeName,
        @Param("classification") String classification,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM spacecraft_type st 
        WHERE (CAST(:typeName AS TEXT) IS NULL OR LOWER(st.type_name) LIKE LOWER(CONCAT('%', CAST(:typeName AS TEXT), '%'))) 
        AND (:classification IS NULL OR st.classification = :classification)
    """)
    long countWithFilters(
        @Param("typeName") String typeName,
        @Param("classification") String classification
    );

    @Query("SELECT st.* FROM spacecraft_type st WHERE st.max_crew_capacity >= :minCrewCapacity")
    List<SpacecraftType> findByMinCrewCapacity(@Param("minCrewCapacity") Integer minCrewCapacity);

    @Query("""
        SELECT DISTINCT st.classification 
        FROM spacecraft_type st 
        ORDER BY st.classification
    """)
    List<String> findAllClassifications();
}