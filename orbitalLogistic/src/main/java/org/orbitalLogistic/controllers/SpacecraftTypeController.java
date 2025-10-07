package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftTypeResponseDTO;
import org.orbitalLogistic.services.SpacecraftTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spacecraft-types")
@RequiredArgsConstructor
public class SpacecraftTypeController {

    private final SpacecraftTypeService spacecraftTypeService;

    @GetMapping
    public ResponseEntity<List<SpacecraftTypeResponseDTO>> getAllSpacecraftTypes() {
        List<SpacecraftTypeResponseDTO> response = spacecraftTypeService.getAllSpacecraftTypes();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpacecraftTypeResponseDTO> getSpacecraftTypeById(@PathVariable Long id) {
        SpacecraftTypeResponseDTO response = spacecraftTypeService.getSpacecraftTypeById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SpacecraftTypeResponseDTO> createSpacecraftType(@Valid @RequestBody SpacecraftTypeRequestDTO request) {
        SpacecraftTypeResponseDTO response = spacecraftTypeService.createSpacecraftType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
