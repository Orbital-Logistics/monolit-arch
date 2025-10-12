package org.orbitalLogistic.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftResponseDTO;
import org.orbitalLogistic.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.exceptions.SpacecraftAlreadyExistsException;
import org.orbitalLogistic.exceptions.SpacecraftNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.services.SpacecraftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacecraftControllerTests {

    @Mock
    private SpacecraftService spacecraftService;

    @InjectMocks
    private SpacecraftController spacecraftController;

    private SpacecraftResponseDTO testSpacecraftResponse;
    private SpacecraftRequestDTO testSpacecraftRequest;
    private PageResponseDTO<SpacecraftResponseDTO> testPageResponse;

    @BeforeEach
    void setUp() {
        testSpacecraftResponse = new SpacecraftResponseDTO(
                1L, "NCC-1701", "Enterprise", "Cargo Ship", SpacecraftClassification.CARGO_HAULER,
                BigDecimal.valueOf(1000.0), BigDecimal.valueOf(500.0),
                SpacecraftStatus.DOCKED, "Earth Orbit",
                BigDecimal.ZERO, BigDecimal.ZERO  // currentMassUsage и currentVolumeUsage в конце
        );

        testSpacecraftRequest = new SpacecraftRequestDTO(
                "NCC-1701", "Enterprise", 1L, BigDecimal.valueOf(1000.0),
                BigDecimal.valueOf(500.0), SpacecraftStatus.DOCKED, "Earth Orbit"
        );

        testPageResponse = new PageResponseDTO<>(
                List.of(testSpacecraftResponse), 0, 20, 1L, 1, true, true
        );
    }

    @Test
    void getAllSpacecrafts_WithValidFilters_ShouldReturnPageResponse() {
        // given
        when(spacecraftService.getSpacecrafts("Enterprise", "DOCKED", 0, 20))
                .thenReturn(testPageResponse);

        // when
        ResponseEntity<PageResponseDTO<SpacecraftResponseDTO>> response = spacecraftController.getAllSpacecrafts(
                "Enterprise", "DOCKED", 0, 20
        );

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
        assertEquals("Enterprise", response.getBody().content().get(0).name());
        verify(spacecraftService, times(1)).getSpacecrafts("Enterprise", "DOCKED", 0, 20);
    }

    @Test
    void getAllSpacecrafts_WithNullFilters_ShouldReturnAllSpacecrafts() {
        // given
        when(spacecraftService.getSpacecrafts(null, null, 0, 20))
                .thenReturn(testPageResponse);

        // when
        ResponseEntity<PageResponseDTO<SpacecraftResponseDTO>> response = spacecraftController.getAllSpacecrafts(
                null, null, 0, 20
        );

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(spacecraftService, times(1)).getSpacecrafts(null, null, 0, 20);
    }

    @Test
    void getAllSpacecrafts_WithDefaultParameters_ShouldUseDefaults() {
        // given
        when(spacecraftService.getSpacecrafts(null, null, 0, 20))
                .thenReturn(testPageResponse);

        // when
        ResponseEntity<PageResponseDTO<SpacecraftResponseDTO>> response = spacecraftController.getAllSpacecrafts(
                null, null, 0, 20
        );

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(spacecraftService, times(1)).getSpacecrafts(null, null, 0, 20);
    }

    @Test
    void getSpacecraftById_WithValidId_ShouldReturnSpacecraft() {
        // given
        when(spacecraftService.getSpacecraftById(1L)).thenReturn(testSpacecraftResponse);

        // when
        ResponseEntity<SpacecraftResponseDTO> response = spacecraftController.getSpacecraftById(1L);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("Enterprise", response.getBody().name());
        verify(spacecraftService, times(1)).getSpacecraftById(1L);
    }

    @Test
    void getSpacecraftById_WithInvalidId_ShouldPropagateException() {
        // given
        when(spacecraftService.getSpacecraftById(999L))
                .thenThrow(new SpacecraftNotFoundException("Spacecraft not found with id: 999"));

        // when & then
        SpacecraftNotFoundException exception = assertThrows(
                SpacecraftNotFoundException.class,
                () -> spacecraftController.getSpacecraftById(999L)
        );

        assertEquals("Spacecraft not found with id: 999", exception.getMessage());
        verify(spacecraftService, times(1)).getSpacecraftById(999L);
    }

    @Test
    void createSpacecraft_WithValidRequest_ShouldReturnCreatedResponse() {
        // given
        when(spacecraftService.createSpacecraft(testSpacecraftRequest)).thenReturn(testSpacecraftResponse);

        // when
        ResponseEntity<SpacecraftResponseDTO> response = spacecraftController.createSpacecraft(testSpacecraftRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Enterprise", response.getBody().name());
        verify(spacecraftService, times(1)).createSpacecraft(testSpacecraftRequest);
    }

    @Test
    void createSpacecraft_WithExistingRegistryCode_ShouldPropagateException() {
        // given
        when(spacecraftService.createSpacecraft(testSpacecraftRequest))
                .thenThrow(new SpacecraftAlreadyExistsException("Spacecraft with registry code already exists: NCC-1701"));

        // when & then
        SpacecraftAlreadyExistsException exception = assertThrows(
                SpacecraftAlreadyExistsException.class,
                () -> spacecraftController.createSpacecraft(testSpacecraftRequest)
        );

        assertEquals("Spacecraft with registry code already exists: NCC-1701", exception.getMessage());
        verify(spacecraftService, times(1)).createSpacecraft(testSpacecraftRequest);
    }

    @Test
    void createSpacecraft_WithInvalidType_ShouldPropagateException() {
        // given
        when(spacecraftService.createSpacecraft(testSpacecraftRequest))
                .thenThrow(new DataNotFoundException("Spacecraft type not found"));

        // when & then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> spacecraftController.createSpacecraft(testSpacecraftRequest)
        );

        assertEquals("Spacecraft type not found", exception.getMessage());
        verify(spacecraftService, times(1)).createSpacecraft(testSpacecraftRequest);
    }

    @Test
    void updateSpacecraft_WithValidId_ShouldReturnUpdatedSpacecraft() {
        // given
        SpacecraftResponseDTO updatedSpacecraft = new SpacecraftResponseDTO(
                1L, "NCC-1701-A", "Enterprise-A", "Cargo Ship", SpacecraftClassification.CARGO_HAULER,
                BigDecimal.valueOf(1200.0), BigDecimal.valueOf(600.0),
                SpacecraftStatus.MAINTENANCE, "Mars Orbit",
                BigDecimal.ZERO, BigDecimal.ZERO  // currentMassUsage и currentVolumeUsage в конце
        );
        when(spacecraftService.updateSpacecraft(1L, testSpacecraftRequest)).thenReturn(updatedSpacecraft);

        // when
        ResponseEntity<SpacecraftResponseDTO> response = spacecraftController.updateSpacecraft(1L, testSpacecraftRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Enterprise-A", response.getBody().name());
        verify(spacecraftService, times(1)).updateSpacecraft(1L, testSpacecraftRequest);
    }

    @Test
    void updateSpacecraft_WithInvalidId_ShouldPropagateException() {
        // given
        when(spacecraftService.updateSpacecraft(999L, testSpacecraftRequest))
                .thenThrow(new SpacecraftNotFoundException("Spacecraft not found with id: 999"));

        // when & then
        SpacecraftNotFoundException exception = assertThrows(
                SpacecraftNotFoundException.class,
                () -> spacecraftController.updateSpacecraft(999L, testSpacecraftRequest)
        );

        assertEquals("Spacecraft not found with id: 999", exception.getMessage());
        verify(spacecraftService, times(1)).updateSpacecraft(999L, testSpacecraftRequest);
    }

    @Test
    void updateSpacecraft_WithExistingRegistryCode_ShouldPropagateException() {
        // given
        when(spacecraftService.updateSpacecraft(1L, testSpacecraftRequest))
                .thenThrow(new SpacecraftAlreadyExistsException("Spacecraft with registry code already exists: NCC-1701"));

        // when & then
        SpacecraftAlreadyExistsException exception = assertThrows(
                SpacecraftAlreadyExistsException.class,
                () -> spacecraftController.updateSpacecraft(1L, testSpacecraftRequest)
        );

        assertEquals("Spacecraft with registry code already exists: NCC-1701", exception.getMessage());
        verify(spacecraftService, times(1)).updateSpacecraft(1L, testSpacecraftRequest);
    }

    @Test
    void deleteSpacecraft_WithValidId_ShouldReturnNoContent() {
        // given
        doNothing().when(spacecraftService).deleteSpacecraft(1L);

        // when
        ResponseEntity<Void> response = spacecraftController.deleteSpacecraft(1L);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(spacecraftService, times(1)).deleteSpacecraft(1L);
    }

    @Test
    void deleteSpacecraft_WithInvalidId_ShouldPropagateException() {
        // given
        doThrow(new SpacecraftNotFoundException("Spacecraft not found with id: 999"))
                .when(spacecraftService).deleteSpacecraft(999L);

        // when & then
        SpacecraftNotFoundException exception = assertThrows(
                SpacecraftNotFoundException.class,
                () -> spacecraftController.deleteSpacecraft(999L)
        );

        assertEquals("Spacecraft not found with id: 999", exception.getMessage());
        verify(spacecraftService, times(1)).deleteSpacecraft(999L);
    }

    @Test
    void getAvailableSpacecrafts_ShouldReturnAvailableSpacecrafts() {
        // given
        List<SpacecraftResponseDTO> availableSpacecrafts = List.of(testSpacecraftResponse);
        when(spacecraftService.getAvailableSpacecrafts()).thenReturn(availableSpacecrafts);

        // when
        ResponseEntity<List<SpacecraftResponseDTO>> response = spacecraftController.getAvailableSpacecrafts();

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Enterprise", response.getBody().get(0).name());
        verify(spacecraftService, times(1)).getAvailableSpacecrafts();
    }

    @Test
    void updateSpacecraftStatus_WithValidId_ShouldReturnUpdatedSpacecraft() {
        // given
        SpacecraftResponseDTO updatedSpacecraft = new SpacecraftResponseDTO(
                1L, "NCC-1701", "Enterprise", "Cargo Ship", SpacecraftClassification.CARGO_HAULER,
                BigDecimal.valueOf(1000.0), BigDecimal.valueOf(500.0),
                SpacecraftStatus.MAINTENANCE, "Earth Orbit",
                BigDecimal.ZERO, BigDecimal.ZERO  // currentMassUsage и currentVolumeUsage в конце
        );
        when(spacecraftService.updateSpacecraftStatus(1L, SpacecraftStatus.MAINTENANCE))
                .thenReturn(updatedSpacecraft);

        // when
        ResponseEntity<SpacecraftResponseDTO> response = spacecraftController.updateSpacecraftStatus(1L, SpacecraftStatus.MAINTENANCE);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SpacecraftStatus.MAINTENANCE, response.getBody().status());
        verify(spacecraftService, times(1)).updateSpacecraftStatus(1L, SpacecraftStatus.MAINTENANCE);
    }

    @Test
    void updateSpacecraftStatus_WithInvalidId_ShouldPropagateException() {
        // given
        when(spacecraftService.updateSpacecraftStatus(999L, SpacecraftStatus.MAINTENANCE))
                .thenThrow(new SpacecraftNotFoundException("Spacecraft not found with id: 999"));

        // when & then
        SpacecraftNotFoundException exception = assertThrows(
                SpacecraftNotFoundException.class,
                () -> spacecraftController.updateSpacecraftStatus(999L, SpacecraftStatus.MAINTENANCE)
        );

        assertEquals("Spacecraft not found with id: 999", exception.getMessage());
        verify(spacecraftService, times(1)).updateSpacecraftStatus(999L, SpacecraftStatus.MAINTENANCE);
    }

    @Test
    void createSpacecraft_WithNullRequest_ShouldPropagateException() {
        // given
        when(spacecraftService.createSpacecraft(null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> spacecraftController.createSpacecraft(null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(spacecraftService, times(1)).createSpacecraft(null);
    }

    @Test
    void updateSpacecraft_WithNullRequest_ShouldPropagateException() {
        // given
        when(spacecraftService.updateSpacecraft(1L, null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> spacecraftController.updateSpacecraft(1L, null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(spacecraftService, times(1)).updateSpacecraft(1L, null);
    }

    @Test
    void getAllSpacecrafts_WithDifferentStatus_ShouldReturnFilteredResults() {
        // given
        when(spacecraftService.getSpacecrafts("Enterprise", "IN_TRANSIT", 0, 20))
                .thenReturn(testPageResponse);

        // when
        ResponseEntity<PageResponseDTO<SpacecraftResponseDTO>> response = spacecraftController.getAllSpacecrafts(
                "Enterprise", "IN_TRANSIT", 0, 20
        );

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(spacecraftService, times(1)).getSpacecrafts("Enterprise", "IN_TRANSIT", 0, 20);
    }

    @Test
    void getAllSpacecrafts_WithDecommissionedStatus_ShouldReturnFilteredResults() {
        // given
        when(spacecraftService.getSpacecrafts(null, "DECOMMISSIONED", 0, 20))
                .thenReturn(testPageResponse);

        // when
        ResponseEntity<PageResponseDTO<SpacecraftResponseDTO>> response = spacecraftController.getAllSpacecrafts(
                null, "DECOMMISSIONED", 0, 20
        );

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(spacecraftService, times(1)).getSpacecrafts(null, "DECOMMISSIONED", 0, 20);
    }

    @Test
    void getAllSpacecrafts_WithDifferentClassification_ShouldReturnFilteredResults() {
        // given
        SpacecraftResponseDTO scienceVesselResponse = new SpacecraftResponseDTO(
                2L, "NCC-74656", "Voyager", "Science Vessel", SpacecraftClassification.SCIENCE_VESSEL,
                BigDecimal.valueOf(800.0), BigDecimal.valueOf(400.0),
                SpacecraftStatus.DOCKED, "Jupiter Orbit",
                BigDecimal.ZERO, BigDecimal.ZERO  // currentMassUsage и currentVolumeUsage в конце
        );
        PageResponseDTO<SpacecraftResponseDTO> sciencePageResponse = new PageResponseDTO<>(
                List.of(scienceVesselResponse), 0, 20, 1L, 1, true, true
        );

        when(spacecraftService.getSpacecrafts(null, null, 0, 20))
                .thenReturn(sciencePageResponse);

        // when
        ResponseEntity<PageResponseDTO<SpacecraftResponseDTO>> response = spacecraftController.getAllSpacecrafts(
                null, null, 0, 20
        );

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
        assertEquals("Voyager", response.getBody().content().get(0).name());
        assertEquals(SpacecraftClassification.SCIENCE_VESSEL, response.getBody().content().get(0).classification());
        verify(spacecraftService, times(1)).getSpacecrafts(null, null, 0, 20);
    }
}
