package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoStorageRequestDTO;
import org.orbitalLogistic.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.services.CargoStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CargoStorageController {

    private final CargoStorageService cargoStorageService;

    @GetMapping("/cargo-storage")
    public ResponseEntity<PageResponseDTO<CargoStorageResponseDTO>> getAllCargoStorage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<CargoStorageResponseDTO> response = cargoStorageService.getAllCargoStorage(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cargo-storage")
    public ResponseEntity<CargoStorageResponseDTO> addCargoToStorage(
            @Valid @RequestBody CargoStorageRequestDTO request) {

        CargoStorageResponseDTO response = cargoStorageService.addCargoToStorage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/cargo-storage/{id}/quantity")
    public ResponseEntity<CargoStorageResponseDTO> updateCargoQuantity(
            @PathVariable Long id,
            @Valid @RequestBody CargoStorageRequestDTO request) {

        CargoStorageResponseDTO response = cargoStorageService.updateQuantity(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/storage-units/{id}/storage")
    public ResponseEntity<PageResponseDTO<CargoStorageResponseDTO>> getStorageUnitCargo(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<CargoStorageResponseDTO> response = cargoStorageService.getStorageUnitCargo(id, page, size);
        return ResponseEntity.ok(response);
    }
}
