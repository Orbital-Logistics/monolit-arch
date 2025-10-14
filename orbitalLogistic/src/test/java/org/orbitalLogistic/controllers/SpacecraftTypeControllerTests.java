package org.orbitalLogistic.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftTypeResponseDTO;
import org.orbitalLogistic.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.exceptions.SpacecraftTypeNotFoundException;
import org.orbitalLogistic.services.SpacecraftTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacecraftTypeControllerTests {

    @Mock
    private SpacecraftTypeService spacecraftTypeService;

    @InjectMocks
    private SpacecraftTypeController spacecraftTypeController;

    private SpacecraftTypeResponseDTO testSpacecraftTypeResponse;
    private SpacecraftTypeRequestDTO testSpacecraftTypeRequest;

    @BeforeEach
    void setUp() {
        testSpacecraftTypeResponse = new SpacecraftTypeResponseDTO(
                1L, "Cargo Ship", SpacecraftClassification.CARGO_HAULER, 10 
        );

        testSpacecraftTypeRequest = new SpacecraftTypeRequestDTO(
                "Cargo Ship", SpacecraftClassification.CARGO_HAULER, 10 
        );
    }

    @Test
    void getAllSpacecraftTypes_ShouldReturnListOfTypes() {
        
        List<SpacecraftTypeResponseDTO> types = List.of(testSpacecraftTypeResponse);
        when(spacecraftTypeService.getAllSpacecraftTypes()).thenReturn(types);

        
        ResponseEntity<List<SpacecraftTypeResponseDTO>> response = spacecraftTypeController.getAllSpacecraftTypes();

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Cargo Ship", response.getBody().get(0).typeName());
        assertEquals(SpacecraftClassification.CARGO_HAULER, response.getBody().get(0).classification()); 
        verify(spacecraftTypeService, times(1)).getAllSpacecraftTypes();
    }

    @Test
    void getAllSpacecraftTypes_WhenNoTypes_ShouldReturnEmptyList() {
        
        when(spacecraftTypeService.getAllSpacecraftTypes()).thenReturn(List.of());

        
        ResponseEntity<List<SpacecraftTypeResponseDTO>> response = spacecraftTypeController.getAllSpacecraftTypes();

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(spacecraftTypeService, times(1)).getAllSpacecraftTypes();
    }

    @Test
    void getAllSpacecraftTypes_WithMultipleTypes_ShouldReturnAllTypes() {
        
        SpacecraftTypeResponseDTO type1 = new SpacecraftTypeResponseDTO(
                1L, "Cargo Ship", SpacecraftClassification.CARGO_HAULER, 10 
        );
        SpacecraftTypeResponseDTO type2 = new SpacecraftTypeResponseDTO(
                2L, "Science Vessel", SpacecraftClassification.SCIENCE_VESSEL, 5 
        );
        List<SpacecraftTypeResponseDTO> types = List.of(type1, type2);
        when(spacecraftTypeService.getAllSpacecraftTypes()).thenReturn(types);

        
        ResponseEntity<List<SpacecraftTypeResponseDTO>> response = spacecraftTypeController.getAllSpacecraftTypes();

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Cargo Ship", response.getBody().get(0).typeName());
        assertEquals("Science Vessel", response.getBody().get(1).typeName());
        verify(spacecraftTypeService, times(1)).getAllSpacecraftTypes();
    }

    @Test
    void getSpacecraftTypeById_WithValidId_ShouldReturnType() {
        
        when(spacecraftTypeService.getSpacecraftTypeById(1L)).thenReturn(testSpacecraftTypeResponse);

        
        ResponseEntity<SpacecraftTypeResponseDTO> response = spacecraftTypeController.getSpacecraftTypeById(1L);

        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("Cargo Ship", response.getBody().typeName());
        assertEquals(SpacecraftClassification.CARGO_HAULER, response.getBody().classification()); 
        verify(spacecraftTypeService, times(1)).getSpacecraftTypeById(1L);
    }

    @Test
    void getSpacecraftTypeById_WithInvalidId_ShouldPropagateException() {
        
        when(spacecraftTypeService.getSpacecraftTypeById(999L))
                .thenThrow(new SpacecraftTypeNotFoundException("Spacecraft type not found with id: 999"));

        
        SpacecraftTypeNotFoundException exception = assertThrows(
                SpacecraftTypeNotFoundException.class,
                () -> spacecraftTypeController.getSpacecraftTypeById(999L)
        );

        assertEquals("Spacecraft type not found with id: 999", exception.getMessage());
        verify(spacecraftTypeService, times(1)).getSpacecraftTypeById(999L);
    }

    @Test
    void createSpacecraftType_WithValidRequest_ShouldReturnCreatedResponse() {
        
        when(spacecraftTypeService.createSpacecraftType(testSpacecraftTypeRequest))
                .thenReturn(testSpacecraftTypeResponse);

        
        ResponseEntity<SpacecraftTypeResponseDTO> response = spacecraftTypeController.createSpacecraftType(testSpacecraftTypeRequest);

        
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cargo Ship", response.getBody().typeName());
        assertEquals(SpacecraftClassification.CARGO_HAULER, response.getBody().classification()); 
        verify(spacecraftTypeService, times(1)).createSpacecraftType(testSpacecraftTypeRequest);
    }

    @Test
    void createSpacecraftType_WithMinimalRequest_ShouldReturnCreatedResponse() {
        
        SpacecraftTypeRequestDTO minimalRequest = new SpacecraftTypeRequestDTO(
                "Minimal Ship", SpacecraftClassification.CARGO_HAULER, 5 
        );
        SpacecraftTypeResponseDTO minimalResponse = new SpacecraftTypeResponseDTO(
                1L, "Minimal Ship", SpacecraftClassification.CARGO_HAULER, 5 
        );
        when(spacecraftTypeService.createSpacecraftType(minimalRequest)).thenReturn(minimalResponse);

        
        ResponseEntity<SpacecraftTypeResponseDTO> response = spacecraftTypeController.createSpacecraftType(minimalRequest);

        
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Minimal Ship", response.getBody().typeName());
        verify(spacecraftTypeService, times(1)).createSpacecraftType(minimalRequest);
    }

    @Test
    void createSpacecraftType_WithFullRequest_ShouldReturnCreatedResponse() {
        
        SpacecraftTypeRequestDTO fullRequest = new SpacecraftTypeRequestDTO(
                "Full Ship", SpacecraftClassification.PERSONNEL_TRANSPORT, 20 
        );
        SpacecraftTypeResponseDTO fullResponse = new SpacecraftTypeResponseDTO(
                1L, "Full Ship", SpacecraftClassification.PERSONNEL_TRANSPORT, 20 
        );
        when(spacecraftTypeService.createSpacecraftType(fullRequest)).thenReturn(fullResponse);

        
        ResponseEntity<SpacecraftTypeResponseDTO> response = spacecraftTypeController.createSpacecraftType(fullRequest);

        
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Full Ship", response.getBody().typeName());
        assertEquals(SpacecraftClassification.PERSONNEL_TRANSPORT, response.getBody().classification());
        assertEquals(20, response.getBody().maxCrewCapacity()); 
        verify(spacecraftTypeService, times(1)).createSpacecraftType(fullRequest);
    }

    @Test
    void createSpacecraftType_WithNullRequest_ShouldPropagateException() {
        
        when(spacecraftTypeService.createSpacecraftType(null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> spacecraftTypeController.createSpacecraftType(null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(spacecraftTypeService, times(1)).createSpacecraftType(null);
    }

    @Test
    void createSpacecraftType_WithServiceException_ShouldPropagateException() {
        
        when(spacecraftTypeService.createSpacecraftType(testSpacecraftTypeRequest))
                .thenThrow(new RuntimeException("Service error"));

        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> spacecraftTypeController.createSpacecraftType(testSpacecraftTypeRequest)
        );

        assertEquals("Service error", exception.getMessage());
        verify(spacecraftTypeService, times(1)).createSpacecraftType(testSpacecraftTypeRequest);
    }

    @Test
    void createSpacecraftType_WithValidationError_ShouldPropagateException() {
        
        when(spacecraftTypeService.createSpacecraftType(testSpacecraftTypeRequest))
                .thenThrow(new IllegalArgumentException("Validation failed"));

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> spacecraftTypeController.createSpacecraftType(testSpacecraftTypeRequest)
        );

        assertEquals("Validation failed", exception.getMessage());
        verify(spacecraftTypeService, times(1)).createSpacecraftType(testSpacecraftTypeRequest);
    }

    @Test
    void getSpacecraftTypeById_WithServiceException_ShouldPropagateException() {
        
        when(spacecraftTypeService.getSpacecraftTypeById(1L))
                .thenThrow(new RuntimeException("Service error"));

        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> spacecraftTypeController.getSpacecraftTypeById(1L)
        );

        assertEquals("Service error", exception.getMessage());
        verify(spacecraftTypeService, times(1)).getSpacecraftTypeById(1L);
    }

    @Test
    void getAllSpacecraftTypes_WithServiceException_ShouldPropagateException() {
        
        when(spacecraftTypeService.getAllSpacecraftTypes())
                .thenThrow(new RuntimeException("Service error"));

        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> spacecraftTypeController.getAllSpacecraftTypes()
        );

        assertEquals("Service error", exception.getMessage());
        verify(spacecraftTypeService, times(1)).getAllSpacecraftTypes();
    }

    @Test
    void createSpacecraftType_WithScienceVessel_ShouldReturnCreatedResponse() {
        
        SpacecraftTypeRequestDTO scienceRequest = new SpacecraftTypeRequestDTO(
                "Science Vessel", SpacecraftClassification.SCIENCE_VESSEL, 15
        );
        SpacecraftTypeResponseDTO scienceResponse = new SpacecraftTypeResponseDTO(
                3L, "Science Vessel", SpacecraftClassification.SCIENCE_VESSEL, 15
        );
        when(spacecraftTypeService.createSpacecraftType(scienceRequest)).thenReturn(scienceResponse);

        
        ResponseEntity<SpacecraftTypeResponseDTO> response = spacecraftTypeController.createSpacecraftType(scienceRequest);

        
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Science Vessel", response.getBody().typeName());
        assertEquals(SpacecraftClassification.SCIENCE_VESSEL, response.getBody().classification());
        assertEquals(15, response.getBody().maxCrewCapacity());
        verify(spacecraftTypeService, times(1)).createSpacecraftType(scienceRequest);
    }
}
