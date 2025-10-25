package org.orbitalLogistic.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.dto.response.MaintenanceLogResponseDTO;
import org.orbitalLogistic.entities.enums.MaintenanceType;
import org.orbitalLogistic.entities.enums.MaintenanceStatus;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.services.MaintenanceLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceLogControllerTests {

    @Mock
    private MaintenanceLogService maintenanceLogService;

    @InjectMocks
    private MaintenanceLogController maintenanceLogController;

    private MaintenanceLogResponseDTO testMaintenanceResponse;
    private MaintenanceLogRequestDTO testMaintenanceRequest;

    @BeforeEach
    void setUp() {
        testMaintenanceResponse = new MaintenanceLogResponseDTO(
                1L,
                "Odyssey-7",
                MaintenanceType.ROUTINE,
                MaintenanceStatus.COMPLETED,
                "techUser",
                "supervisorA",
                LocalDateTime.of(2025, 1, 10, 10, 0),
                LocalDateTime.of(2025, 1, 10, 15, 0),
                "Routine inspection completed",
                new BigDecimal("1500.00")
        );

        testMaintenanceRequest = new MaintenanceLogRequestDTO(
                1L,
                MaintenanceType.ROUTINE,
                100L,
                200L,
                LocalDateTime.of(2025, 1, 10, 10, 0),
                LocalDateTime.of(2025, 1, 10, 15, 0),
                "Checking life support systems",
                new BigDecimal("900.00"),
                SpacecraftStatus.DOCKED,
                new BigDecimal("950.00"),
                "All systems operational",
                300L
        );
    }

    @Test
    void getAllMaintenanceLogs_ShouldReturnPageOfLogs() {
        var page = new PageResponseDTO<>(
                List.of(testMaintenanceResponse),
                0,
                20,
                1,
                1,
                true,
                true
        );

        when(maintenanceLogService.getAllMaintenanceLogs(0, 20)).thenReturn(page);

        ResponseEntity<PageResponseDTO<MaintenanceLogResponseDTO>> response =
                maintenanceLogController.getAllMaintenanceLogs(0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().content().size());
        assertEquals("Odyssey-7", response.getBody().content().get(0).spacecraftName());
        verify(maintenanceLogService, times(1)).getAllMaintenanceLogs(0, 20);
    }

    @Test
    void createMaintenanceLog_ShouldReturnCreatedResponse() {
        when(maintenanceLogService.createMaintenanceLog(testMaintenanceRequest))
                .thenReturn(testMaintenanceResponse);

        ResponseEntity<MaintenanceLogResponseDTO> response =
                maintenanceLogController.createMaintenanceLog(testMaintenanceRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Odyssey-7", response.getBody().spacecraftName());
        assertEquals(MaintenanceStatus.COMPLETED, response.getBody().status());
        verify(maintenanceLogService, times(1)).createMaintenanceLog(testMaintenanceRequest);
    }

    @Test
    void updateMaintenanceStatus_ShouldReturnUpdatedResponse() {
        var updatedResponse = new MaintenanceLogResponseDTO(
                1L,
                "Odyssey-7",
                MaintenanceType.REPAIR,
                MaintenanceStatus.COMPLETED,
                "techUser2",
                "supervisorB",
                LocalDateTime.of(2025, 2, 1, 9, 0),
                LocalDateTime.of(2025, 2, 1, 13, 0),
                "Replaced thermal sensors",
                new BigDecimal("1750.00")
        );

        when(maintenanceLogService.updateMaintenanceStatus(eq(1L), any(MaintenanceLogRequestDTO.class)))
                .thenReturn(updatedResponse);

        ResponseEntity<MaintenanceLogResponseDTO> response =
                maintenanceLogController.updateMaintenanceStatus(1L, testMaintenanceRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MaintenanceType.REPAIR, response.getBody().maintenanceType());
        assertEquals(MaintenanceStatus.COMPLETED, response.getBody().status());
        assertEquals("techUser2", response.getBody().performedByUserName());
        verify(maintenanceLogService, times(1))
                .updateMaintenanceStatus(eq(1L), any(MaintenanceLogRequestDTO.class));
    }

    @Test
    void getSpacecraftMaintenanceHistory_ShouldReturnLogs() {
        var page = new PageResponseDTO<>(
                List.of(testMaintenanceResponse),
                0,
                20,
                1,
                1,
                true,
                true
        );

        when(maintenanceLogService.getSpacecraftMaintenanceHistory(10L, 0, 20))
                .thenReturn(page);

        ResponseEntity<PageResponseDTO<MaintenanceLogResponseDTO>> response =
                maintenanceLogController.getSpacecraftMaintenanceHistory(10L, 0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Odyssey-7", response.getBody().content().get(0).spacecraftName());
        verify(maintenanceLogService, times(1))
                .getSpacecraftMaintenanceHistory(10L, 0, 20);
    }

    @Test
    void createMaintenanceLog_WithInvalidRequest_ShouldThrowException() {
        when(maintenanceLogService.createMaintenanceLog(any()))
                .thenThrow(new IllegalArgumentException("Invalid maintenance data"));

        var ex = assertThrows(IllegalArgumentException.class, () ->
                maintenanceLogController.createMaintenanceLog(testMaintenanceRequest)
        );

        assertEquals("Invalid maintenance data", ex.getMessage());
        verify(maintenanceLogService, times(1)).createMaintenanceLog(any());
    }
}
