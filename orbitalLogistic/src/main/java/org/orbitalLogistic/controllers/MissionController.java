package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MissionRequestDTO;
import org.orbitalLogistic.dto.response.MissionResponseDTO;
import org.orbitalLogistic.services.MissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<MissionResponseDTO>> getAllMissions(
            @RequestParam(required = false) String missionCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String missionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<MissionResponseDTO> response = missionService.getMissions(missionCode, status, missionType, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MissionResponseDTO> getMissionById(@PathVariable Long id) {
        MissionResponseDTO response = missionService.getMissionById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MissionResponseDTO> createMission(@Valid @RequestBody MissionRequestDTO request) {
        MissionResponseDTO response = missionService.createMission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<MissionResponseDTO> updateMission(
            @PathVariable Long id,
            @Valid @RequestBody MissionRequestDTO request) {

        MissionResponseDTO response = missionService.updateMission(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<MissionResponseDTO> completeMission(
            @PathVariable Long id,
            @Valid @RequestBody MissionRequestDTO request) {

        MissionResponseDTO response = missionService.completeMission(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<MissionResponseDTO>> getActiveMissions() {
        List<MissionResponseDTO> response = missionService.getActiveMissions();
        return ResponseEntity.ok(response);
    }
}
