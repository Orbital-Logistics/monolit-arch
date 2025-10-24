package org.orbitalLogistic.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoManifestRequestDTO;
import org.orbitalLogistic.dto.response.CargoManifestResponseDTO;
import org.orbitalLogistic.entities.enums.ManifestPriority;
import org.orbitalLogistic.entities.enums.ManifestStatus;
import org.orbitalLogistic.services.CargoManifestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoManifestControllerTests {

    @Mock
    private CargoManifestService cargoManifestService;

    @InjectMocks
    private CargoManifestController cargoManifestController;

    private CargoManifestResponseDTO testManifestResponse;
    private CargoManifestRequestDTO testManifestRequest;
    private PageResponseDTO<CargoManifestResponseDTO> testPageResponse;

    @BeforeEach
    void setUp() {
        testManifestResponse = new CargoManifestResponseDTO(
                1L,
                "Starship",
                "Food Supplies",
                "STU-001",
                42,
                ManifestStatus.LOADED,
                ManifestPriority.HIGH,
                LocalDateTime.now(),
                null,
                "john_doe",
                null
        );

        testManifestRequest = new CargoManifestRequestDTO(
                1L,
                2L,
                3L,
                42,
                ManifestPriority.HIGH,
                10L,
                List.of(),
                4L,
                null,
                LocalDateTime.now(),
                null,
                "LOAD",
                "Initial load operation"
        );

        testPageResponse = new PageResponseDTO<>(
                List.of(testManifestResponse),
                0,
                20,
                1L,
                1,
                true,
                true
        );
    }

    @Test
    void getAllManifests_ShouldReturnPageResponse() {
        when(cargoManifestService.getAllManifests(0, 20)).thenReturn(testPageResponse);

        ResponseEntity<PageResponseDTO<CargoManifestResponseDTO>> response =
                cargoManifestController.getAllManifests(0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().content().size());
        verify(cargoManifestService, times(1)).getAllManifests(0, 20);
    }

    @Test
    void getManifestById_ShouldReturnManifest() {
        when(cargoManifestService.getManifestById(1L)).thenReturn(testManifestResponse);

        ResponseEntity<CargoManifestResponseDTO> response =
                cargoManifestController.getManifestById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Starship", response.getBody().spacecraftName());
        verify(cargoManifestService, times(1)).getManifestById(1L);
    }

    @Test
    void loadCargoToSpacecraft_ShouldReturnCreatedList() {
        when(cargoManifestService.loadCargoToSpacecraft(eq(1L), any(CargoManifestRequestDTO.class)))
                .thenReturn(List.of(testManifestResponse));

        ResponseEntity<List<CargoManifestResponseDTO>> response =
                cargoManifestController.loadCargoToSpacecraft(1L, testManifestRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Food Supplies", response.getBody().get(0).cargoName());
        verify(cargoManifestService, times(1))
                .loadCargoToSpacecraft(eq(1L), eq(testManifestRequest));
    }

    @Test
    void unloadCargoFromSpacecraft_ShouldReturnList() {
        when(cargoManifestService.unloadCargoFromSpacecraft(eq(1L), any(CargoManifestRequestDTO.class)))
                .thenReturn(List.of(testManifestResponse));

        ResponseEntity<List<CargoManifestResponseDTO>> response =
                cargoManifestController.unloadCargoFromSpacecraft(1L, testManifestRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(cargoManifestService, times(1))
                .unloadCargoFromSpacecraft(eq(1L), eq(testManifestRequest));
    }

    @Test
    void getSpacecraftManifest_ShouldReturnPageResponse() {
        when(cargoManifestService.getSpacecraftManifest(1L, 0, 20))
                .thenReturn(testPageResponse);

        ResponseEntity<PageResponseDTO<CargoManifestResponseDTO>> response =
                cargoManifestController.getSpacecraftManifest(1L, 0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().content().size());
        verify(cargoManifestService, times(1))
                .getSpacecraftManifest(1L, 0, 20);
    }

    @Test
    void loadCargoToSpacecraft_WithNullRequest_ShouldThrowException() {
        when(cargoManifestService.loadCargoToSpacecraft(1L, null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cargoManifestController.loadCargoToSpacecraft(1L, null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(cargoManifestService, times(1))
                .loadCargoToSpacecraft(1L, null);
    }

    @Test
    void unloadCargoFromSpacecraft_WithInvalidId_ShouldThrowException() {
        when(cargoManifestService.unloadCargoFromSpacecraft(eq(999L), any()))
                .thenThrow(new IllegalArgumentException("Spacecraft not found"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cargoManifestController.unloadCargoFromSpacecraft(999L, testManifestRequest)
        );

        assertEquals("Spacecraft not found", exception.getMessage());
        verify(cargoManifestService, times(1))
                .unloadCargoFromSpacecraft(999L, testManifestRequest);
    }
}
