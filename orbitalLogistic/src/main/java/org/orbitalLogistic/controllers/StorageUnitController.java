package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.dto.response.StorageUnitResponseDTO;
import org.orbitalLogistic.services.StorageUnitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storage-units")
@RequiredArgsConstructor
public class StorageUnitController {

    private final StorageUnitService storageUnitService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<StorageUnitResponseDTO>> getAllStorageUnits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponseDTO<StorageUnitResponseDTO> response = storageUnitService.getStorageUnits(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorageUnitResponseDTO> getStorageUnitById(@PathVariable Long id) {
        StorageUnitResponseDTO response = storageUnitService.getStorageUnitById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<StorageUnitResponseDTO> createStorageUnit(@Valid @RequestBody StorageUnitRequestDTO request) {
        StorageUnitResponseDTO response = storageUnitService.createStorageUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StorageUnitResponseDTO> updateStorageUnit(
            @PathVariable Long id,
            @Valid @RequestBody StorageUnitRequestDTO request) {
        
        StorageUnitResponseDTO response = storageUnitService.updateStorageUnit(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/inventory")
    public ResponseEntity<PageResponseDTO<CargoStorageResponseDTO>> getStorageUnitInventory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponseDTO<CargoStorageResponseDTO> response = storageUnitService.getStorageUnitInventory(id, page, size);
        return ResponseEntity.ok(response);
    }
}