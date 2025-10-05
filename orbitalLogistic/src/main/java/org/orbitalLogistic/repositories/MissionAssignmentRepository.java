package org.orbitalLogistic.repositories;

import org.orbitalLogistic.entities.MissionAssignment;
import org.orbitalLogistic.entities.enums.AssignmentRole;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MissionAssignmentRepository extends CrudRepository<MissionAssignment, Long> {

    List<MissionAssignment> findByMissionId(Long missionId);
    List<MissionAssignment> findByUserId(Long userId);
    List<MissionAssignment> findByAssignmentRole(AssignmentRole assignmentRole);

    List<MissionAssignment> findByMissionIdAndUserId(Long missionId, Long userId);
    List<MissionAssignment> findByMissionIdAndAssignmentRole(Long missionId, AssignmentRole assignmentRole);

    @Query("""
        SELECT ma.* FROM mission_assignment ma
        INNER JOIN mission m ON ma.mission_id = m.id
        WHERE ma.user_id = :userId
        AND m.status IN ('SCHEDULED', 'IN_PROGRESS')
        ORDER BY m.scheduled_departure NULLS LAST
    """)
    List<MissionAssignment> findActiveAssignmentsByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT ma.* FROM mission_assignment ma
        WHERE ma.mission_id = :missionId
        ORDER BY ma.assignment_role, ma.assigned_at
    """)
    List<MissionAssignment> findByMissionIdOrderByRole(@Param("missionId") Long missionId);

    @Query("""
        SELECT ma.* FROM mission_assignment ma
        INNER JOIN mission m ON ma.mission_id = m.id
        WHERE ma.assigned_at BETWEEN :startDate AND :endDate
        ORDER BY ma.assigned_at DESC
    """)
    List<MissionAssignment> findByAssignedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COUNT(*) FROM mission_assignment ma
        WHERE ma.mission_id = :missionId
        AND ma.assignment_role = :role
    """)
    long countByMissionIdAndRole(@Param("missionId") Long missionId, @Param("role") String role);

    boolean existsByMissionIdAndUserId(Long missionId, Long userId);
}
