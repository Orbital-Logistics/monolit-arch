package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoRequestDTO;
import org.orbitalLogistic.dto.response.CargoResponseDTO;
import org.orbitalLogistic.services.CargoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    @GetMapping
    public ResponseEntity<List<CargoResponseDTO>> getAllCargos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (size > 50) size = 50;

        List<CargoResponseDTO> response = cargoService.getCargosScroll(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paged")
    public ResponseEntity<PageResponseDTO<CargoResponseDTO>> getAllCargosPaged(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cargoType,
            @RequestParam(required = false) String hazardLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (size > 50) size = 50;

        PageResponseDTO<CargoResponseDTO> response = cargoService.getCargosPaged(name, cargoType, hazardLevel, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.totalElements()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoResponseDTO> getCargoById(@PathVariable Long id) {
        CargoResponseDTO response = cargoService.getCargoById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CargoResponseDTO> createCargo(@Valid @RequestBody CargoRequestDTO request) {
        CargoResponseDTO response = cargoService.createCargo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CargoResponseDTO> updateCargo(
            @PathVariable Long id,
            @Valid @RequestBody CargoRequestDTO request) {
        
        CargoResponseDTO response = cargoService.updateCargo(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCargo(@PathVariable Long id) {
        cargoService.deleteCargo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<CargoResponseDTO>> searchCargos(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cargoType,
            @RequestParam(required = false) String hazardLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (size > 50) size = 50;

        PageResponseDTO<CargoResponseDTO> response = cargoService.searchCargos(name, cargoType, hazardLevel, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.totalElements()))
                .body(response);
    }
}
