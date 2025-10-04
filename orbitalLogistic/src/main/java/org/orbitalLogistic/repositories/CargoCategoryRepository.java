package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.CargoCategory;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargoCategoryRepository extends CrudRepository<CargoCategory, Long> {

    Optional<CargoCategory> findByName(String name);
    boolean existsByName(String name);
    
    List<CargoCategory> findByParentCategoryIdIsNull();
    List<CargoCategory> findByParentCategoryId(Long parentCategoryId);

    @Query("""
        SELECT cc.* FROM cargo_category cc 
        WHERE (:name IS NULL OR LOWER(cc.name) LIKE LOWER(CONCAT('%', :name, '%')))
        ORDER BY cc.name
        LIMIT :limit OFFSET :offset
    """)
    List<CargoCategory> findWithFilters(
        @Param("name") String name,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM cargo_category cc 
        WHERE (:name IS NULL OR LOWER(cc.name) LIKE LOWER(CONCAT('%', :name, '%')))
    """)
    long countWithFilters(@Param("name") String name);

    @Query("""
        WITH RECURSIVE category_tree AS (
            SELECT id, name, parent_category_id, 1 as level
            FROM cargo_category 
            WHERE id = :categoryId
            UNION ALL
            SELECT cc.id, cc.name, cc.parent_category_id, ct.level + 1
            FROM cargo_category cc
            INNER JOIN category_tree ct ON cc.parent_category_id = ct.id
        )
        SELECT * FROM category_tree ORDER BY level
    """)
    List<CargoCategory> findCategoryTree(@Param("categoryId") Long categoryId);

    @Query("SELECT cc.* FROM cargo_category cc WHERE LOWER(cc.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<CargoCategory> searchByName(@Param("searchTerm") String searchTerm);
}