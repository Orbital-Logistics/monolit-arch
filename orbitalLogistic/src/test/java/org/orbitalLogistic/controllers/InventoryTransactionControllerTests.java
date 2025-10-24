package org.orbitalLogistic.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.dto.response.InventoryTransactionResponseDTO;
import org.orbitalLogistic.entities.enums.TransactionType;
import org.orbitalLogistic.services.InventoryTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class InventoryTransactionControllerTests {

    @Mock
    private InventoryTransactionService inventoryTransactionService;

    @InjectMocks
    private InventoryTransactionController inventoryTransactionController;

    private InventoryTransactionResponseDTO testTransactionResponse;
    private InventoryTransactionRequestDTO testTransactionRequest;
    private PageResponseDTO<InventoryTransactionResponseDTO> testPageResponse;

    @BeforeEach
    void setUp() {
        testTransactionResponse = new InventoryTransactionResponseDTO(
                1L,
                TransactionType.TRANSFER,
                "Oxygen Tanks",
                10,
                "Storage A",
                "Storage B",
                "operator",
                LocalDateTime.of(2025, 1, 15, 9, 30),
                "RC-01",
                "REF-12345",
                "Routine transfer"
        );

        
        testTransactionRequest = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 
                100L,                     
                10,                       
                10L,                      
                20L,                      
                null,                     
                null,                     
                999L,                     
                "RC-01",                  
                "REF-12345",              
                "Routine transfer"        
        );

        testPageResponse = new PageResponseDTO<>(
                List.of(testTransactionResponse),
                0,
                20,
                1L,
                1,
                true,
                true
        );
    }

    @Test
    void getAllTransactions_ShouldReturnPage() {
        when(inventoryTransactionService.getAllTransactions(0, 20)).thenReturn(testPageResponse);

        ResponseEntity<PageResponseDTO<InventoryTransactionResponseDTO>> response =
                inventoryTransactionController.getAllTransactions(0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().content().size());
        assertEquals("Oxygen Tanks", response.getBody().content().get(0).cargoName());
        verify(inventoryTransactionService, times(1)).getAllTransactions(0, 20);
    }

    @Test
    void getTransactionById_ShouldReturnTransaction() {
        when(inventoryTransactionService.getTransactionById(1L)).thenReturn(testTransactionResponse);

        ResponseEntity<InventoryTransactionResponseDTO> response =
                inventoryTransactionController.getTransactionById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TransactionType.TRANSFER, response.getBody().transactionType());
        assertEquals("Oxygen Tanks", response.getBody().cargoName());
        verify(inventoryTransactionService, times(1)).getTransactionById(1L);
    }

    @Test
    void transferCargo_ShouldReturnCreatedTransaction() {
        when(inventoryTransactionService.transferBetweenStorages(any(InventoryTransactionRequestDTO.class)))
                .thenReturn(testTransactionResponse);

        ResponseEntity<InventoryTransactionResponseDTO> response =
                inventoryTransactionController.transferCargo(testTransactionRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("REF-12345", response.getBody().referenceNumber());
        verify(inventoryTransactionService, times(1))
                .transferBetweenStorages(eq(testTransactionRequest));
    }

    @Test
    void getCargoHistory_ShouldReturnPage() {
        when(inventoryTransactionService.getCargoHistory(100L, 0, 20)).thenReturn(testPageResponse);

        ResponseEntity<PageResponseDTO<InventoryTransactionResponseDTO>> response =
                inventoryTransactionController.getCargoHistory(100L, 0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().content().size());
        assertEquals(10, response.getBody().content().get(0).quantity());
        verify(inventoryTransactionService, times(1)).getCargoHistory(100L, 0, 20);
    }

    @Test
    void transferCargo_WithInvalidRequest_ShouldPropagateException() {
        when(inventoryTransactionService.transferBetweenStorages(any()))
                .thenThrow(new IllegalArgumentException("Invalid transfer request"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryTransactionController.transferCargo(testTransactionRequest)
        );

        assertEquals("Invalid transfer request", ex.getMessage());
        verify(inventoryTransactionService, times(1))
                .transferBetweenStorages(eq(testTransactionRequest));
    }
}
