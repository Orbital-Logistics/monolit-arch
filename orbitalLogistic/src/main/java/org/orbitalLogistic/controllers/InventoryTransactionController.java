package org.orbitalLogistic.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.dto.response.InventoryTransactionResponseDTO;
import org.orbitalLogistic.services.InventoryTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory-transactions")
@RequiredArgsConstructor
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<InventoryTransactionResponseDTO>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<InventoryTransactionResponseDTO> response = inventoryTransactionService.getAllTransactions(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryTransactionResponseDTO> getTransactionById(@PathVariable Long id) {
        InventoryTransactionResponseDTO response = inventoryTransactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<InventoryTransactionResponseDTO> transferCargo(
            @Valid @RequestBody InventoryTransactionRequestDTO request) {

        InventoryTransactionResponseDTO response = inventoryTransactionService.transferBetweenStorages(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/cargo/{cargoId}")
    public ResponseEntity<PageResponseDTO<InventoryTransactionResponseDTO>> getCargoHistory(
            @PathVariable Long cargoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<InventoryTransactionResponseDTO> response = inventoryTransactionService.getCargoHistory(cargoId, page, size);
        return ResponseEntity.ok(response);
    }
}
