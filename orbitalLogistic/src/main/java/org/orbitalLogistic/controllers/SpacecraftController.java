package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftResponseDTO;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.services.SpacecraftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spacecrafts")
@RequiredArgsConstructor
public class SpacecraftController {

    private final SpacecraftService spacecraftService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<SpacecraftResponseDTO>> getAllSpacecrafts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponseDTO<SpacecraftResponseDTO> response = spacecraftService.getSpacecrafts(name, status, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpacecraftResponseDTO> getSpacecraftById(@PathVariable Long id) {
        SpacecraftResponseDTO response = spacecraftService.getSpacecraftById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SpacecraftResponseDTO> createSpacecraft(@Valid @RequestBody SpacecraftRequestDTO request) {
        SpacecraftResponseDTO response = spacecraftService.createSpacecraft(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpacecraftResponseDTO> updateSpacecraft(
            @PathVariable Long id, 
            @Valid @RequestBody SpacecraftRequestDTO request) {
        
        SpacecraftResponseDTO response = spacecraftService.updateSpacecraft(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<List<SpacecraftResponseDTO>> getAvailableSpacecrafts() {
        List<SpacecraftResponseDTO> response = spacecraftService.getAvailableSpacecrafts();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SpacecraftResponseDTO> updateSpacecraftStatus(
            @PathVariable Long id,
            @RequestParam SpacecraftStatus status) {
        
        SpacecraftResponseDTO response = spacecraftService.updateSpacecraftStatus(id, status);
        return ResponseEntity.ok(response);
    }
}