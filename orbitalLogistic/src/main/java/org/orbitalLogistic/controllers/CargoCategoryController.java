package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.request.CargoCategoryRequestDTO;
import org.orbitalLogistic.dto.response.CargoCategoryResponseDTO;
import org.orbitalLogistic.services.CargoCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargo-categories")
@RequiredArgsConstructor
public class CargoCategoryController {

    private final CargoCategoryService cargoCategoryService;

    @GetMapping
    public ResponseEntity<List<CargoCategoryResponseDTO>> getAllCategories() {
        List<CargoCategoryResponseDTO> response = cargoCategoryService.getAllCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoCategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        CargoCategoryResponseDTO response = cargoCategoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CargoCategoryResponseDTO> createCategory(@Valid @RequestBody CargoCategoryRequestDTO request) {
        CargoCategoryResponseDTO response = cargoCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CargoCategoryResponseDTO>> getCategoryTree() {
        List<CargoCategoryResponseDTO> response = cargoCategoryService.getCategoryTree();
        return ResponseEntity.ok(response);
    }
}
