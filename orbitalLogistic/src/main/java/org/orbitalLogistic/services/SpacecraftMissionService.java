package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.request.SpacecraftMissionRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftMissionResponseDTO;
import org.orbitalLogistic.entities.SpacecraftMission;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.Mission;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.SpacecraftMissionMapper;
import org.orbitalLogistic.repositories.SpacecraftMissionRepository;
import org.orbitalLogistic.repositories.SpacecraftRepository;
import org.orbitalLogistic.repositories.MissionRepository;
import org.orbitalLogistic.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SpacecraftMissionService {

    private final SpacecraftMissionRepository spacecraftMissionRepository;
    private final SpacecraftRepository spacecraftRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final SpacecraftMissionMapper spacecraftMissionMapper;

    @Transactional(readOnly = true)
    public List<SpacecraftMissionResponseDTO> getMissionBackupSpacecrafts(Long missionId) {
        List<SpacecraftMission> spacecraftMissions = spacecraftMissionRepository.findByMissionIdOrderBySpacecraftName(missionId);
        return spacecraftMissions.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public SpacecraftMissionResponseDTO addBackupSpacecraft(Long missionId, SpacecraftMissionRequestDTO request) {
        validateEntities(missionId, request);

        // Check if spacecraft is already assigned to this mission
        if (spacecraftMissionRepository.existsBySpacecraftIdAndMissionId(request.spacecraftId(), missionId)) {
            throw new RuntimeException("Spacecraft is already assigned to this mission");
        }

        SpacecraftMission spacecraftMission = SpacecraftMission.builder()
                .spacecraftId(request.spacecraftId())
                .missionId(missionId)
                .build();

        SpacecraftMission saved = spacecraftMissionRepository.save(spacecraftMission);
        return toResponseDTO(saved, request.roleDescription(), request.assignedByUserId());
    }

    @Transactional
    public void removeBackupSpacecraft(Long missionId, Long spacecraftId) {
        if (!spacecraftMissionRepository.existsBySpacecraftIdAndMissionId(spacecraftId, missionId)) {
            throw new DataNotFoundException("Spacecraft assignment not found for this mission");
        }
        spacecraftMissionRepository.deleteBySpacecraftIdAndMissionId(spacecraftId, missionId);
    }

    private void validateEntities(Long missionId, SpacecraftMissionRequestDTO request) {
        missionRepository.findById(missionId)
                .orElseThrow(() -> new DataNotFoundException("Mission not found"));

        spacecraftRepository.findById(request.spacecraftId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found"));

        userRepository.findById(request.assignedByUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found"));
    }

    private SpacecraftMissionResponseDTO toResponseDTO(SpacecraftMission spacecraftMission) {
        return toResponseDTO(spacecraftMission, null, null);
    }

    private SpacecraftMissionResponseDTO toResponseDTO(SpacecraftMission spacecraftMission,
                                                      String roleDescription, Long assignedByUserId) {
        Spacecraft spacecraft = spacecraftRepository.findById(spacecraftMission.getSpacecraftId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found"));

        Mission mission = missionRepository.findById(spacecraftMission.getMissionId())
                .orElseThrow(() -> new DataNotFoundException("Mission not found"));

        String assignedByUserName = null;
        if (assignedByUserId != null) {
            User user = userRepository.findById(assignedByUserId).orElse(null);
            if (user != null) {
                assignedByUserName = user.getUsername();
            }
        }

        return spacecraftMissionMapper.toResponseDTO(spacecraftMission,
                spacecraft.getName(),
                spacecraft.getRegistryCode(),
                mission.getMissionName(),
                mission.getMissionCode(),
                roleDescription,
                assignedByUserName);
    }
}
