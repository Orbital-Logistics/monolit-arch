package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.dto.response.MissionAssignmentResponseDTO;
import org.orbitalLogistic.services.MissionAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mission-assignments")
@RequiredArgsConstructor
public class MissionAssignmentController {

    private final MissionAssignmentService missionAssignmentService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<MissionAssignmentResponseDTO>> getAllAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<MissionAssignmentResponseDTO> response = missionAssignmentService.getAllAssignments(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/missions/{missionId}")
    public ResponseEntity<PageResponseDTO<MissionAssignmentResponseDTO>> getMissionAssignments(
            @PathVariable Long missionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<MissionAssignmentResponseDTO> response = missionAssignmentService.getMissionAssignments(missionId, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/missions/{missionId}/assign-crew")
    public ResponseEntity<List<MissionAssignmentResponseDTO>> assignCrew(
            @PathVariable Long missionId,
            @Valid @RequestBody MissionAssignmentRequestDTO request) {

        List<MissionAssignmentResponseDTO> response = missionAssignmentService.assignCrew(missionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> removeAssignment(@PathVariable Long assignmentId) {
        missionAssignmentService.removeAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }
}
