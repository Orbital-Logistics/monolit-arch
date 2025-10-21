package org.orbitalLogistic.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoStorageRequestDTO;
import org.orbitalLogistic.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.services.CargoStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoStorageControllerTests {

    @Mock
    private CargoStorageService cargoStorageService;

    @InjectMocks
    private CargoStorageController cargoStorageController;

    private CargoStorageResponseDTO testStorageResponse;
    private CargoStorageRequestDTO testStorageRequest;

    @BeforeEach
    void setUp() {
        testStorageResponse = new CargoStorageResponseDTO(
                1L,
                "STU-001",
                "Central Storage",
                "Oxygen Tanks",
                50,
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 5, 12, 0),
                "admin"
        );

        testStorageRequest = new CargoStorageRequestDTO(
                200L,            
                100L,            
                50,              
                999L,            
                "Initial stock", 
                "ADD"            
        );
    }

    @Test
    void getAllCargoStorage_ShouldReturnPageOfStorage() {
        var page = new PageResponseDTO<>(
                List.of(testStorageResponse),
                0,
                20,
                1,
                1,
                true,
                true
        );
        when(cargoStorageService.getAllCargoStorage(0, 20)).thenReturn(page);

        ResponseEntity<PageResponseDTO<CargoStorageResponseDTO>> response =
                cargoStorageController.getAllCargoStorage(0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().content().size());
        assertEquals("Oxygen Tanks", response.getBody().content().get(0).cargoName());
        verify(cargoStorageService, times(1)).getAllCargoStorage(0, 20);
    }

    @Test
    void addCargoToStorage_ShouldReturnCreatedResponse() {
        when(cargoStorageService.addCargoToStorage(testStorageRequest)).thenReturn(testStorageResponse);

        ResponseEntity<CargoStorageResponseDTO> response =
                cargoStorageController.addCargoToStorage(testStorageRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Oxygen Tanks", response.getBody().cargoName());
        verify(cargoStorageService, times(1)).addCargoToStorage(testStorageRequest);
    }

    @Test
    void updateCargoQuantity_ShouldReturnUpdatedStorage() {
        var updatedResponse = new CargoStorageResponseDTO(
                1L,
                "STU-001",
                "Central Storage",
                "Oxygen Tanks",
                80,
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                "inspector"
        );
        when(cargoStorageService.updateQuantity(eq(1L), any(CargoStorageRequestDTO.class)))
                .thenReturn(updatedResponse);

        ResponseEntity<CargoStorageResponseDTO> response =
                cargoStorageController.updateCargoQuantity(1L, testStorageRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(80, response.getBody().quantity());
        assertEquals("inspector", response.getBody().lastCheckedByUserName());
        verify(cargoStorageService, times(1)).updateQuantity(eq(1L), any(CargoStorageRequestDTO.class));
    }

    @Test
    void getStorageUnitCargo_ShouldReturnPageOfStorageForUnit() {
        var page = new PageResponseDTO<>(
                List.of(testStorageResponse),
                0,
                20,
                1,
                1,
                true,
                true
        );
        when(cargoStorageService.getStorageUnitCargo(10L, 0, 20)).thenReturn(page);

        ResponseEntity<PageResponseDTO<CargoStorageResponseDTO>> response =
                cargoStorageController.getStorageUnitCargo(10L, 0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().content().size());
        assertEquals("Central Storage", response.getBody().content().get(0).storageLocation());
        verify(cargoStorageService, times(1)).getStorageUnitCargo(10L, 0, 20);
    }

    @Test
    void addCargoToStorage_WithInvalidRequest_ShouldPropagateException() {
        when(cargoStorageService.addCargoToStorage(any()))
                .thenThrow(new IllegalArgumentException("Invalid storage request"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cargoStorageController.addCargoToStorage(testStorageRequest)
        );

        assertEquals("Invalid storage request", exception.getMessage());
        verify(cargoStorageService, times(1)).addCargoToStorage(any());
    }
}
