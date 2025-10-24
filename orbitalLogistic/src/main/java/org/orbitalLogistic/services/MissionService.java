package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MissionRequestDTO;
import org.orbitalLogistic.dto.response.MissionResponseDTO;
import org.orbitalLogistic.entities.Mission;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.enums.MissionStatus;
import org.orbitalLogistic.exceptions.MissionNotFoundException;
import org.orbitalLogistic.exceptions.MissionAlreadyExistsException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.MissionMapper;
import org.orbitalLogistic.repositories.MissionRepository;
import org.orbitalLogistic.repositories.UserRepository;
import org.orbitalLogistic.repositories.SpacecraftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final SpacecraftRepository spacecraftRepository;
    private final MissionMapper missionMapper;

    @Transactional(readOnly = true)
    public PageResponseDTO<MissionResponseDTO> getMissions(String missionCode, String status, String missionType, int page, int size) {
        int offset = page * size;
        List<Mission> missions = missionRepository.findWithFilters(missionCode, null, status, missionType, null, null, null, size, offset);
        long total = missionRepository.countWithFilters(missionCode, null, status, missionType, null, null, null);

        List<MissionResponseDTO> missionDTOs = missions.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(missionDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    @Transactional(readOnly = true)
    public MissionResponseDTO getMissionById(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));
        return toResponseDTO(mission);
    }

    @Transactional(readOnly = true)
    public List<MissionResponseDTO> getActiveMissions() {
        LocalDateTime now = LocalDateTime.now();
        return missionRepository.findActiveMissions(now).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public MissionResponseDTO createMission(MissionRequestDTO request) {
        if (missionRepository.existsByMissionCode(request.missionCode())) {
            throw new MissionAlreadyExistsException("Mission with code already exists: " + request.missionCode());
        }

        // Validate commanding officer exists
        User commandingOfficer = userRepository.findById(request.commandingOfficerId())
                .orElseThrow(() -> new DataNotFoundException("Commanding officer not found"));

        // Validate spacecraft exists
        Spacecraft spacecraft = spacecraftRepository.findById(request.spacecraftId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found"));

        Mission mission = missionMapper.toEntity(request);
        Mission saved = missionRepository.save(mission);
        return toResponseDTO(saved);
    }

    public MissionResponseDTO updateMission(Long id, MissionRequestDTO request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        if (!mission.getMissionCode().equals(request.missionCode()) &&
            missionRepository.existsByMissionCode(request.missionCode())) {
            throw new MissionAlreadyExistsException("Mission with code already exists: " + request.missionCode());
        }

        // Update mission fields
        mission.setMissionCode(request.missionCode());
        mission.setMissionName(request.missionName());
        mission.setMissionType(request.missionType());
        mission.setPriority(request.priority());
        mission.setCommandingOfficerId(request.commandingOfficerId());
        mission.setSpacecraftId(request.spacecraftId());
        mission.setScheduledDeparture(request.scheduledDeparture());
        mission.setScheduledArrival(request.scheduledArrival());

        Mission updated = missionRepository.save(mission);
        return toResponseDTO(updated);
    }

    @Transactional
    public MissionResponseDTO completeMission(Long id, MissionRequestDTO request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));

        // Complete mission
        mission.setStatus(request.isSuccessful() ? MissionStatus.COMPLETED : MissionStatus.CANCELLED);
        // Additional completion logic would go here

        Mission updated = missionRepository.save(mission);
        return toResponseDTO(updated);
    }

    private MissionResponseDTO toResponseDTO(Mission mission) {
        User commandingOfficer = userRepository.findById(mission.getCommandingOfficerId())
                .orElseThrow(() -> new DataNotFoundException("Commanding officer not found"));

        Spacecraft spacecraft = spacecraftRepository.findById(mission.getSpacecraftId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found"));

        return missionMapper.toResponseDTO(mission,
                commandingOfficer.getUsername() + " " + commandingOfficer.getUsername(),
                spacecraft.getName());
    }
}
