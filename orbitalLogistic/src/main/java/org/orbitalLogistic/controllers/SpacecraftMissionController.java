package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.request.SpacecraftMissionRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftMissionResponseDTO;
import org.orbitalLogistic.services.SpacecraftMissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class SpacecraftMissionController {

    private final SpacecraftMissionService spacecraftMissionService;

    @PostMapping("/{id}/backup-spacecraft")
    public ResponseEntity<SpacecraftMissionResponseDTO> addBackupSpacecraft(
            @PathVariable Long id,
            @Valid @RequestBody SpacecraftMissionRequestDTO request) {

        SpacecraftMissionResponseDTO response = spacecraftMissionService.addBackupSpacecraft(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{missionId}/backup-spacecraft/{spacecraftId}")
    public ResponseEntity<Void> removeBackupSpacecraft(
            @PathVariable Long missionId,
            @PathVariable Long spacecraftId) {

        spacecraftMissionService.removeBackupSpacecraft(missionId, spacecraftId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/backup-spacecrafts")
    public ResponseEntity<List<SpacecraftMissionResponseDTO>> getMissionBackupSpacecrafts(@PathVariable Long id) {
        List<SpacecraftMissionResponseDTO> response = spacecraftMissionService.getMissionBackupSpacecrafts(id);
        return ResponseEntity.ok(response);
    }
}
