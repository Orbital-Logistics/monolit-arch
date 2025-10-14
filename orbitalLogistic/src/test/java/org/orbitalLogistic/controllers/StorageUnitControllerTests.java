package org.orbitalLogistic.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.dto.response.StorageUnitResponseDTO;
import org.orbitalLogistic.entities.enums.StorageType;
import org.orbitalLogistic.exceptions.StorageUnitAlreadyExistsException;
import org.orbitalLogistic.exceptions.StorageUnitNotFoundException;
import org.orbitalLogistic.services.StorageUnitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageUnitControllerTests {

    @Mock
    private StorageUnitService storageUnitService;

    @InjectMocks
    private StorageUnitController storageUnitController;

    private StorageUnitResponseDTO testStorageUnitResponse;
    private StorageUnitRequestDTO testStorageUnitRequest;
    private PageResponseDTO<StorageUnitResponseDTO> testPageResponse;

    @BeforeEach
    void setUp() {
        testStorageUnitResponse = new StorageUnitResponseDTO(
                1L, "SU-001", "Warehouse A", StorageType.AMBIENT,
                BigDecimal.valueOf(1000.0), BigDecimal.valueOf(500.0),
                BigDecimal.valueOf(200.0), BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(800.0), BigDecimal.valueOf(400.0),
                20.0, 20.0
        );

        testStorageUnitRequest = new StorageUnitRequestDTO(
                "SU-001", "Warehouse A", StorageType.AMBIENT
                ,
                BigDecimal.valueOf(1000.0), BigDecimal.valueOf(500.0)
        );

        testPageResponse = new PageResponseDTO<>(
                List.of(testStorageUnitResponse), 0, 20, 1L, 1, true, true
        );
    }

    @Test
    void getAllStorageUnits_WithValidParameters_ShouldReturnPageResponse() {
        
        when(storageUnitService.getStorageUnits(0, 20)).thenReturn(testPageResponse);

        
        ResponseEntity<PageResponseDTO<StorageUnitResponseDTO>> response = storageUnitController.getAllStorageUnits(0, 20);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
        assertEquals("SU-001", response.getBody().content().get(0).unitCode());
        verify(storageUnitService, times(1)).getStorageUnits(0, 20);
    }

    @Test
    void getAllStorageUnits_WithDefaultParameters_ShouldUseDefaults() {
        
        when(storageUnitService.getStorageUnits(0, 20)).thenReturn(testPageResponse);

        
        ResponseEntity<PageResponseDTO<StorageUnitResponseDTO>> response = storageUnitController.getAllStorageUnits(0, 20);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(storageUnitService, times(1)).getStorageUnits(0, 20);
    }

    @Test
    void getAllStorageUnits_WithCustomParameters_ShouldPassCorrectly() {
        
        when(storageUnitService.getStorageUnits(2, 10)).thenReturn(testPageResponse);

        
        ResponseEntity<PageResponseDTO<StorageUnitResponseDTO>> response = storageUnitController.getAllStorageUnits(2, 10);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(storageUnitService, times(1)).getStorageUnits(2, 10);
    }

    @Test
    void getStorageUnitById_WithValidId_ShouldReturnStorageUnit() {
        
        when(storageUnitService.getStorageUnitById(1L)).thenReturn(testStorageUnitResponse);

        
        ResponseEntity<StorageUnitResponseDTO> response = storageUnitController.getStorageUnitById(1L);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("SU-001", response.getBody().unitCode());
        assertEquals("Warehouse A", response.getBody().location());
        verify(storageUnitService, times(1)).getStorageUnitById(1L);
    }

    @Test
    void getStorageUnitById_WithInvalidId_ShouldPropagateException() {
        
        when(storageUnitService.getStorageUnitById(999L))
                .thenThrow(new StorageUnitNotFoundException("Storage unit not found with id: 999"));

        
        StorageUnitNotFoundException exception = assertThrows(
                StorageUnitNotFoundException.class,
                () -> storageUnitController.getStorageUnitById(999L)
        );

        assertEquals("Storage unit not found with id: 999", exception.getMessage());
        verify(storageUnitService, times(1)).getStorageUnitById(999L);
    }

    @Test
    void createStorageUnit_WithValidRequest_ShouldReturnCreatedResponse() {
        
        when(storageUnitService.createStorageUnit(testStorageUnitRequest))
                .thenReturn(testStorageUnitResponse);

        
        ResponseEntity<StorageUnitResponseDTO> response = storageUnitController.createStorageUnit(testStorageUnitRequest);

        
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SU-001", response.getBody().unitCode());
        assertEquals("Warehouse A", response.getBody().location());
        verify(storageUnitService, times(1)).createStorageUnit(testStorageUnitRequest);
    }

    @Test
    void createStorageUnit_WithExistingUnitCode_ShouldPropagateException() {
        
        when(storageUnitService.createStorageUnit(testStorageUnitRequest))
                .thenThrow(new StorageUnitAlreadyExistsException("Storage unit with code already exists: SU-001"));

        
        StorageUnitAlreadyExistsException exception = assertThrows(
                StorageUnitAlreadyExistsException.class,
                () -> storageUnitController.createStorageUnit(testStorageUnitRequest)
        );

        assertEquals("Storage unit with code already exists: SU-001", exception.getMessage());
        verify(storageUnitService, times(1)).createStorageUnit(testStorageUnitRequest);
    }

    @Test
    void createStorageUnit_WithNullRequest_ShouldPropagateException() {
        
        when(storageUnitService.createStorageUnit(null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> storageUnitController.createStorageUnit(null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(storageUnitService, times(1)).createStorageUnit(null);
    }

    @Test
    void updateStorageUnit_WithValidId_ShouldReturnUpdatedStorageUnit() {
        
        StorageUnitResponseDTO updatedStorageUnit = new StorageUnitResponseDTO(
                1L, "SU-001-UPDATED", "Warehouse B", StorageType.AMBIENT,
                BigDecimal.valueOf(1200.0), BigDecimal.valueOf(600.0),
                BigDecimal.valueOf(200.0), BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(1000.0), BigDecimal.valueOf(500.0),
                16.67, 16.67
        );
        when(storageUnitService.updateStorageUnit(1L, testStorageUnitRequest))
                .thenReturn(updatedStorageUnit);

        
        ResponseEntity<StorageUnitResponseDTO> response = storageUnitController.updateStorageUnit(1L, testStorageUnitRequest);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SU-001-UPDATED", response.getBody().unitCode());
        assertEquals("Warehouse B", response.getBody().location());
        assertEquals(StorageType.AMBIENT, response.getBody().storageType());
        verify(storageUnitService, times(1)).updateStorageUnit(1L, testStorageUnitRequest);
    }

    @Test
    void updateStorageUnit_WithInvalidId_ShouldPropagateException() {
        
        when(storageUnitService.updateStorageUnit(999L, testStorageUnitRequest))
                .thenThrow(new StorageUnitNotFoundException("Storage unit not found with id: 999"));

        
        StorageUnitNotFoundException exception = assertThrows(
                StorageUnitNotFoundException.class,
                () -> storageUnitController.updateStorageUnit(999L, testStorageUnitRequest)
        );

        assertEquals("Storage unit not found with id: 999", exception.getMessage());
        verify(storageUnitService, times(1)).updateStorageUnit(999L, testStorageUnitRequest);
    }

    @Test
    void updateStorageUnit_WithExistingUnitCode_ShouldPropagateException() {
        
        when(storageUnitService.updateStorageUnit(1L, testStorageUnitRequest))
                .thenThrow(new StorageUnitAlreadyExistsException("Storage unit with code already exists: SU-001"));

        
        StorageUnitAlreadyExistsException exception = assertThrows(
                StorageUnitAlreadyExistsException.class,
                () -> storageUnitController.updateStorageUnit(1L, testStorageUnitRequest)
        );

        assertEquals("Storage unit with code already exists: SU-001", exception.getMessage());
        verify(storageUnitService, times(1)).updateStorageUnit(1L, testStorageUnitRequest);
    }

    @Test
    void updateStorageUnit_WithNullRequest_ShouldPropagateException() {
        
        when(storageUnitService.updateStorageUnit(1L, null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> storageUnitController.updateStorageUnit(1L, null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(storageUnitService, times(1)).updateStorageUnit(1L, null);
    }

    @Test
    void getStorageUnitInventory_WithValidId_ShouldReturnInventory() {
        
        when(storageUnitService.getStorageUnitInventory(1L, 0, 20))
                .thenReturn(testPageResponse);

        
        ResponseEntity<PageResponseDTO<StorageUnitResponseDTO>> response = storageUnitController.getStorageUnitInventory(1L, 0, 20);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
        verify(storageUnitService, times(1)).getStorageUnitInventory(1L, 0, 20);
    }

    @Test
    void getStorageUnitInventory_WithDefaultParameters_ShouldUseDefaults() {
        
        when(storageUnitService.getStorageUnitInventory(1L, 0, 20))
                .thenReturn(testPageResponse);

        
        ResponseEntity<PageResponseDTO<StorageUnitResponseDTO>> response = storageUnitController.getStorageUnitInventory(1L, 0, 20);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(storageUnitService, times(1)).getStorageUnitInventory(1L, 0, 20);
    }

    @Test
    void getStorageUnitInventory_WithCustomParameters_ShouldPassCorrectly() {
        
        when(storageUnitService.getStorageUnitInventory(1L, 2, 10))
                .thenReturn(testPageResponse);

        
        ResponseEntity<PageResponseDTO<StorageUnitResponseDTO>> response = storageUnitController.getStorageUnitInventory(1L, 2, 10);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(storageUnitService, times(1)).getStorageUnitInventory(1L, 2, 10);
    }

    @Test
    void getStorageUnitInventory_WithServiceException_ShouldPropagateException() {
        
        when(storageUnitService.getStorageUnitInventory(1L, 0, 20))
                .thenThrow(new RuntimeException("Service error"));

        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> storageUnitController.getStorageUnitInventory(1L, 0, 20)
        );

        assertEquals("Service error", exception.getMessage());
        verify(storageUnitService, times(1)).getStorageUnitInventory(1L, 0, 20);
    }

    @Test
    void getAllStorageUnits_WithServiceException_ShouldPropagateException() {
        
        when(storageUnitService.getStorageUnits(0, 20))
                .thenThrow(new RuntimeException("Service error"));

        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> storageUnitController.getAllStorageUnits(0, 20)
        );

        assertEquals("Service error", exception.getMessage());
        verify(storageUnitService, times(1)).getStorageUnits(0, 20);
    }

    @Test
    void getStorageUnitById_WithServiceException_ShouldPropagateException() {
        
        when(storageUnitService.getStorageUnitById(1L))
                .thenThrow(new RuntimeException("Service error"));

        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> storageUnitController.getStorageUnitById(1L)
        );

        assertEquals("Service error", exception.getMessage());
        verify(storageUnitService, times(1)).getStorageUnitById(1L);
    }
}
