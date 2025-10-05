package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.SpacecraftMission;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpacecraftMissionRepository extends CrudRepository<SpacecraftMission, Long> {

    List<SpacecraftMission> findBySpacecraftId(Long spacecraftId);
    List<SpacecraftMission> findByMissionId(Long missionId);

    boolean existsBySpacecraftIdAndMissionId(Long spacecraftId, Long missionId);

    @Query("""
        SELECT sm.* FROM spacecraft_mission sm
        INNER JOIN mission m ON sm.mission_id = m.id
        WHERE sm.spacecraft_id = :spacecraftId
        AND m.status IN ('SCHEDULED', 'IN_PROGRESS')
        ORDER BY m.scheduled_departure NULLS LAST
    """)
    List<SpacecraftMission> findActiveAssignmentsBySpacecraftId(@Param("spacecraftId") Long spacecraftId);

    @Query("""
        SELECT sm.* FROM spacecraft_mission sm
        INNER JOIN spacecraft s ON sm.spacecraft_id = s.id
        WHERE sm.mission_id = :missionId
        ORDER BY s.name
    """)
    List<SpacecraftMission> findByMissionIdOrderBySpacecraftName(@Param("missionId") Long missionId);

    @Query("""
        SELECT sm.spacecraft_id FROM spacecraft_mission sm
        INNER JOIN mission m ON sm.mission_id = m.id
        WHERE m.status IN ('SCHEDULED', 'IN_PROGRESS')
    """)
    List<Long> findSpacecraftIdsInActiveMissions();

    @Query("""
        SELECT sm.mission_id FROM spacecraft_mission sm
        INNER JOIN spacecraft s ON sm.spacecraft_id = s.id
        WHERE s.status = 'DOCKED'
        AND sm.spacecraft_id = :spacecraftId
    """)
    List<Long> findAvailableMissionsBySpacecraftId(@Param("spacecraftId") Long spacecraftId);

    @Query("""
        DELETE FROM spacecraft_mission 
        WHERE spacecraft_id = :spacecraftId AND mission_id = :missionId
    """)
    void deleteBySpacecraftIdAndMissionId(@Param("spacecraftId") Long spacecraftId, @Param("missionId") Long missionId);

    @Query("""
        DELETE FROM spacecraft_mission 
        WHERE mission_id = :missionId
    """)
    void deleteByMissionId(@Param("missionId") Long missionId);

    @Query("""
        DELETE FROM spacecraft_mission 
        WHERE spacecraft_id = :spacecraftId
    """)
    void deleteBySpacecraftId(@Param("spacecraftId") Long spacecraftId);
}
