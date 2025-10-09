package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.dto.response.MaintenanceLogResponseDTO;
import org.orbitalLogistic.services.MaintenanceLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MaintenanceLogController {

    private final MaintenanceLogService maintenanceLogService;

    @GetMapping("/maintenance-logs")
    public ResponseEntity<PageResponseDTO<MaintenanceLogResponseDTO>> getAllMaintenanceLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<MaintenanceLogResponseDTO> response = maintenanceLogService.getAllMaintenanceLogs(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/maintenance-logs")
    public ResponseEntity<MaintenanceLogResponseDTO> createMaintenanceLog(
            @Valid @RequestBody MaintenanceLogRequestDTO request) {

        MaintenanceLogResponseDTO response = maintenanceLogService.createMaintenanceLog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/maintenance-logs/{id}/status")
    public ResponseEntity<MaintenanceLogResponseDTO> updateMaintenanceStatus(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceLogRequestDTO request) {

        MaintenanceLogResponseDTO response = maintenanceLogService.updateMaintenanceStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/spacecrafts/{id}/maintenance")
    public ResponseEntity<PageResponseDTO<MaintenanceLogResponseDTO>> getSpacecraftMaintenanceHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<MaintenanceLogResponseDTO> response = maintenanceLogService.getSpacecraftMaintenanceHistory(id, page, size);
        return ResponseEntity.ok(response);
    }
}
