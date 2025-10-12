package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoManifestRequestDTO;
import org.orbitalLogistic.dto.response.CargoManifestResponseDTO;
import org.orbitalLogistic.services.CargoManifestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CargoManifestController {

    private final CargoManifestService cargoManifestService;

    @GetMapping("/cargo-manifests")
    public ResponseEntity<PageResponseDTO<CargoManifestResponseDTO>> getAllManifests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<CargoManifestResponseDTO> response = cargoManifestService.getAllManifests(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cargo-manifests/{id}")
    public ResponseEntity<CargoManifestResponseDTO> getManifestById(@PathVariable Long id) {
        CargoManifestResponseDTO response = cargoManifestService.getManifestById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/spacecrafts/{id}/load-cargo")
    public ResponseEntity<List<CargoManifestResponseDTO>> loadCargoToSpacecraft(
            @PathVariable Long id,
            @Valid @RequestBody CargoManifestRequestDTO request) {

        List<CargoManifestResponseDTO> response = cargoManifestService.loadCargoToSpacecraft(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/spacecrafts/{id}/unload-cargo")
    public ResponseEntity<List<CargoManifestResponseDTO>> unloadCargoFromSpacecraft(
            @PathVariable Long id,
            @Valid @RequestBody CargoManifestRequestDTO request) {

        List<CargoManifestResponseDTO> response = cargoManifestService.unloadCargoFromSpacecraft(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/spacecrafts/{id}/manifest")
    public ResponseEntity<PageResponseDTO<CargoManifestResponseDTO>> getSpacecraftManifest(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<CargoManifestResponseDTO> response = cargoManifestService.getSpacecraftManifest(id, page, size);
        return ResponseEntity.ok(response);
    }
}
