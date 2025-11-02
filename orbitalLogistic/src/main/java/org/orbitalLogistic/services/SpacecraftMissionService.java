package org.orbitalLogistic.services;

import org.orbitalLogistic.dto.request.SpacecraftMissionRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftMissionResponseDTO;
import org.orbitalLogistic.entities.SpacecraftMission;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.Mission;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.exceptions.SpacecraftAssignedMissionException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.SpacecraftMissionMapper;
import org.orbitalLogistic.repositories.SpacecraftMissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpacecraftMissionService {

    private final SpacecraftMissionRepository spacecraftMissionRepository;
    private final SpacecraftMissionMapper spacecraftMissionMapper;

    private SpacecraftService spacecraftService;
    private MissionService missionService;
    private UserService userService;

    public SpacecraftMissionService(SpacecraftMissionRepository spacecraftMissionRepository,
                                   SpacecraftMissionMapper spacecraftMissionMapper) {
        this.spacecraftMissionRepository = spacecraftMissionRepository;
        this.spacecraftMissionMapper = spacecraftMissionMapper;
    }

    @Autowired
    public void setSpacecraftService(@Lazy SpacecraftService spacecraftService) {
        this.spacecraftService = spacecraftService;
    }

    @Autowired
    public void setMissionService(@Lazy MissionService missionService) {
        this.missionService = missionService;
    }

    @Autowired
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    public List<SpacecraftMissionResponseDTO> getMissionBackupSpacecrafts(Long missionId) {
        List<SpacecraftMission> spacecraftMissions = spacecraftMissionRepository.findByMissionIdOrderBySpacecraftName(missionId);
        return spacecraftMissions.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public SpacecraftMissionResponseDTO addBackupSpacecraft(Long missionId, SpacecraftMissionRequestDTO request) {
        validateEntities(missionId, request);

        if (spacecraftMissionRepository.existsBySpacecraftIdAndMissionId(request.spacecraftId(), missionId)) {
            throw new SpacecraftAssignedMissionException("Spacecraft is already assigned to this mission");
        }

        SpacecraftMission spacecraftMission = SpacecraftMission.builder()
                .spacecraftId(request.spacecraftId())
                .missionId(missionId)
                .build();

        SpacecraftMission saved = spacecraftMissionRepository.save(spacecraftMission);
        return toResponseDTO(saved, request.roleDescription(), request.assignedByUserId());
    }

    public void removeBackupSpacecraft(Long missionId, Long spacecraftId) {
        if (!spacecraftMissionRepository.existsBySpacecraftIdAndMissionId(spacecraftId, missionId)) {
            throw new DataNotFoundException("Spacecraft assignment not found for this mission");
        }
        spacecraftMissionRepository.deleteBySpacecraftIdAndMissionId(spacecraftId, missionId);
    }

    private void validateEntities(Long missionId, SpacecraftMissionRequestDTO request) {
        missionService.getEntityById(missionId);
        spacecraftService.getEntityById(request.spacecraftId());
        userService.getEntityById(request.assignedByUserId());
    }

    private SpacecraftMissionResponseDTO toResponseDTO(SpacecraftMission spacecraftMission) {
        return toResponseDTO(spacecraftMission, null, null);
    }

    private SpacecraftMissionResponseDTO toResponseDTO(SpacecraftMission spacecraftMission,
                                                      String roleDescription, Long assignedByUserId) {
        Spacecraft spacecraft = spacecraftService.getEntityById(spacecraftMission.getSpacecraftId());
        Mission mission = missionService.getEntityById(spacecraftMission.getMissionId());

        String assignedByUserName = null;
        if (assignedByUserId != null) {
            User user = userService.getEntityByIdOrNull(assignedByUserId);
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
