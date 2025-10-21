package org.orbitalLogistic.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoRequestDTO;
import org.orbitalLogistic.dto.response.CargoResponseDTO;
import org.orbitalLogistic.entities.enums.CargoType;
import org.orbitalLogistic.entities.enums.HazardLevel;
import org.orbitalLogistic.exceptions.CargoAlreadyExistsException;
import org.orbitalLogistic.exceptions.CargoNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.services.CargoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoControllerTests {

    @Mock
    private CargoService cargoService;

    @InjectMocks
    private CargoController cargoController;

    private CargoResponseDTO testCargoResponse;
    private CargoRequestDTO testCargoRequest;
    private PageResponseDTO<CargoResponseDTO> testPageResponse;

    @BeforeEach
    void setUp() {
        testCargoResponse = new CargoResponseDTO(
                1L, "Scientific Equipment", "Electronics",
                BigDecimal.valueOf(10.5), BigDecimal.valueOf(5.0),
                CargoType.SCIENTIFIC, HazardLevel.LOW, 0
        );

        testCargoRequest = new CargoRequestDTO(
                "Scientific Equipment", 1L,
                BigDecimal.valueOf(10.5), BigDecimal.valueOf(5.0),
                CargoType.SCIENTIFIC, HazardLevel.LOW
        );

        testPageResponse = new PageResponseDTO<>(
                List.of(testCargoResponse), 0, 20, 1L, 1, true, true
        );
    }

    @Test
    void getAllCargos_WithValidParameters_ShouldReturnList() {
        
        List<CargoResponseDTO> cargos = List.of(testCargoResponse);
        when(cargoService.getCargosScroll(0, 20)).thenReturn(cargos);

        
        ResponseEntity<List<CargoResponseDTO>> response = cargoController.getAllCargos(0, 20);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Scientific Equipment", response.getBody().get(0).name());
        verify(cargoService, times(1)).getCargosScroll(0, 20);
    }

    @Test
    void getAllCargos_WithDefaultParameters_ShouldUseDefaults() {
        
        List<CargoResponseDTO> cargos = List.of(testCargoResponse);
        when(cargoService.getCargosScroll(0, 20)).thenReturn(cargos);

        
        ResponseEntity<List<CargoResponseDTO>> response = cargoController.getAllCargos(0, 20);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cargoService, times(1)).getCargosScroll(0, 20);
    }

    @Test
    void getAllCargosPaged_WithValidFilters_ShouldReturnPageResponse() {
        
        when(cargoService.getCargosPaged("Scientific", "SCIENTIFIC", "LOW", 0, 20))
                .thenReturn(testPageResponse);

        
        ResponseEntity<PageResponseDTO<CargoResponseDTO>> response = cargoController.getAllCargosPaged(
                "Scientific", "SCIENTIFIC", "LOW", 0, 20
        );

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
        verify(cargoService, times(1)).getCargosPaged("Scientific", "SCIENTIFIC", "LOW", 0, 20);
    }

    @Test
    void getAllCargosPaged_WithNullFilters_ShouldReturnAllCargos() {
        
        when(cargoService.getCargosPaged(null, null, null, 0, 20))
                .thenReturn(testPageResponse);

        
        ResponseEntity<PageResponseDTO<CargoResponseDTO>> response = cargoController.getAllCargosPaged(
                null, null, null, 0, 20
        );

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cargoService, times(1)).getCargosPaged(null, null, null, 0, 20);
    }

    @Test
    void getCargoById_WithValidId_ShouldReturnCargo() {
        
        when(cargoService.getCargoById(1L)).thenReturn(testCargoResponse);

        
        ResponseEntity<CargoResponseDTO> response = cargoController.getCargoById(1L);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("Scientific Equipment", response.getBody().name());
        verify(cargoService, times(1)).getCargoById(1L);
    }

    @Test
    void getCargoById_WithInvalidId_ShouldPropagateException() {
        
        when(cargoService.getCargoById(999L))
                .thenThrow(new CargoNotFoundException("Cargo not found with id: 999"));

        
        CargoNotFoundException exception = assertThrows(
                CargoNotFoundException.class,
                () -> cargoController.getCargoById(999L)
        );

        assertEquals("Cargo not found with id: 999", exception.getMessage());
        verify(cargoService, times(1)).getCargoById(999L);
    }

    @Test
    void createCargo_WithValidRequest_ShouldReturnCreatedResponse() {
        
        when(cargoService.createCargo(testCargoRequest)).thenReturn(testCargoResponse);

        
        ResponseEntity<CargoResponseDTO> response = cargoController.createCargo(testCargoRequest);

        
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Scientific Equipment", response.getBody().name());
        verify(cargoService, times(1)).createCargo(testCargoRequest);
    }

    @Test
    void createCargo_WithExistingName_ShouldPropagateException() {
        
        when(cargoService.createCargo(testCargoRequest))
                .thenThrow(new CargoAlreadyExistsException("Cargo with name already exists: Scientific Equipment"));

        
        CargoAlreadyExistsException exception = assertThrows(
                CargoAlreadyExistsException.class,
                () -> cargoController.createCargo(testCargoRequest)
        );

        assertEquals("Cargo with name already exists: Scientific Equipment", exception.getMessage());
        verify(cargoService, times(1)).createCargo(testCargoRequest);
    }

    @Test
    void createCargo_WithInvalidCategory_ShouldPropagateException() {
        
        when(cargoService.createCargo(testCargoRequest))
                .thenThrow(new DataNotFoundException("Cargo category not found"));

        
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> cargoController.createCargo(testCargoRequest)
        );

        assertEquals("Cargo category not found", exception.getMessage());
        verify(cargoService, times(1)).createCargo(testCargoRequest);
    }

    @Test
    void updateCargo_WithValidId_ShouldReturnUpdatedCargo() {
        
        CargoResponseDTO updatedCargo = new CargoResponseDTO(
                1L, "Updated Equipment", "Electronics",
                BigDecimal.valueOf(15.0), BigDecimal.valueOf(7.0),
                CargoType.EQUIPMENT, HazardLevel.MEDIUM, 0
        );
        when(cargoService.updateCargo(1L, testCargoRequest)).thenReturn(updatedCargo);

        
        ResponseEntity<CargoResponseDTO> response = cargoController.updateCargo(1L, testCargoRequest);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Equipment", response.getBody().name());
        verify(cargoService, times(1)).updateCargo(1L, testCargoRequest);
    }

    @Test
    void updateCargo_WithInvalidId_ShouldPropagateException() {
        
        when(cargoService.updateCargo(999L, testCargoRequest))
                .thenThrow(new CargoNotFoundException("Cargo not found with id: 999"));

        
        CargoNotFoundException exception = assertThrows(
                CargoNotFoundException.class,
                () -> cargoController.updateCargo(999L, testCargoRequest)
        );

        assertEquals("Cargo not found with id: 999", exception.getMessage());
        verify(cargoService, times(1)).updateCargo(999L, testCargoRequest);
    }

    @Test
    void updateCargo_WithExistingName_ShouldPropagateException() {
        
        when(cargoService.updateCargo(1L, testCargoRequest))
                .thenThrow(new CargoAlreadyExistsException("Cargo with name already exists: Scientific Equipment"));

        
        CargoAlreadyExistsException exception = assertThrows(
                CargoAlreadyExistsException.class,
                () -> cargoController.updateCargo(1L, testCargoRequest)
        );

        assertEquals("Cargo with name already exists: Scientific Equipment", exception.getMessage());
        verify(cargoService, times(1)).updateCargo(1L, testCargoRequest);
    }

    @Test
    void deleteCargo_WithValidId_ShouldReturnNoContent() {
        
        doNothing().when(cargoService).deleteCargo(1L);

        
        ResponseEntity<Void> response = cargoController.deleteCargo(1L);

        
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(cargoService, times(1)).deleteCargo(1L);
    }

    @Test
    void deleteCargo_WithInvalidId_ShouldPropagateException() {
        
        doThrow(new CargoNotFoundException("Cargo not found with id: 999"))
                .when(cargoService).deleteCargo(999L);

        
        CargoNotFoundException exception = assertThrows(
                CargoNotFoundException.class,
                () -> cargoController.deleteCargo(999L)
        );

        assertEquals("Cargo not found with id: 999", exception.getMessage());
        verify(cargoService, times(1)).deleteCargo(999L);
    }

    @Test
    void searchCargos_WithValidFilters_ShouldReturnPageResponse() {
        
        when(cargoService.searchCargos("Scientific", "SCIENTIFIC", "LOW", 0, 20))
                .thenReturn(testPageResponse);

        
        ResponseEntity<PageResponseDTO<CargoResponseDTO>> response = cargoController.searchCargos(
                "Scientific", "SCIENTIFIC", "LOW", 0, 20
        );

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
        verify(cargoService, times(1)).searchCargos("Scientific", "SCIENTIFIC", "LOW", 0, 20);
    }

    @Test
    void searchCargos_WithNullFilters_ShouldReturnAllCargos() {
        
        when(cargoService.searchCargos(null, null, null, 0, 20))
                .thenReturn(testPageResponse);

        
        ResponseEntity<PageResponseDTO<CargoResponseDTO>> response = cargoController.searchCargos(
                null, null, null, 0, 20
        );

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cargoService, times(1)).searchCargos(null, null, null, 0, 20);
    }

    @Test
    void createCargo_WithNullRequest_ShouldPropagateException() {
        
        when(cargoService.createCargo(null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cargoController.createCargo(null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(cargoService, times(1)).createCargo(null);
    }

    @Test
    void updateCargo_WithNullRequest_ShouldPropagateException() {
        
        when(cargoService.updateCargo(1L, null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cargoController.updateCargo(1L, null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(cargoService, times(1)).updateCargo(1L, null);
    }
}
