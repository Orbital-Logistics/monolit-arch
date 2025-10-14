package org.orbitalLogistic.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.dto.response.MissionAssignmentResponseDTO;
import org.orbitalLogistic.entities.enums.AssignmentRole;
import org.orbitalLogistic.services.MissionAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionAssignmentControllerTests {

    @Mock
    private MissionAssignmentService missionAssignmentService;

    @InjectMocks
    private MissionAssignmentController missionAssignmentController;

    private MissionAssignmentResponseDTO testAssignmentResponse;
    private MissionAssignmentRequestDTO testAssignmentRequest;

    @BeforeEach
    void setUp() {
        testAssignmentResponse = new MissionAssignmentResponseDTO(
                1L,
                101L,
                "Europa Exploration",
                "John Doe",
                AssignmentRole.PILOT,
                "Navigation Systems",
                LocalDateTime.of(2025, 3, 1, 12, 0)
        );

        testAssignmentRequest = new MissionAssignmentRequestDTO(
                101L,                     
                10L,                      
                AssignmentRole.ENGINEER,  
                "Propulsion systems",     
                null                       
        );
    }

    @Test
    void getAllAssignments_ShouldReturnPageOfAssignments() {
        var page = new PageResponseDTO<>(
                List.of(testAssignmentResponse),
                0,
                20,
                1,
                1,
                true,
                true
        );

        when(missionAssignmentService.getAllAssignments(0, 20)).thenReturn(page);

        ResponseEntity<PageResponseDTO<MissionAssignmentResponseDTO>> response =
                missionAssignmentController.getAllAssignments(0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().content().size());
        assertEquals("John Doe", response.getBody().content().get(0).userName());
        verify(missionAssignmentService, times(1)).getAllAssignments(0, 20);
    }

    @Test
    void getMissionAssignments_ShouldReturnPageOfAssignmentsForMission() {
        var page = new PageResponseDTO<>(
                List.of(testAssignmentResponse),
                0,
                20,
                1,
                1,
                true,
                true
        );

        when(missionAssignmentService.getMissionAssignments(101L, 0, 20))
                .thenReturn(page);

        ResponseEntity<PageResponseDTO<MissionAssignmentResponseDTO>> response =
                missionAssignmentController.getMissionAssignments(101L, 0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Europa Exploration", response.getBody().content().get(0).missionName());
        verify(missionAssignmentService, times(1))
                .getMissionAssignments(101L, 0, 20);
    }

    @Test
    void assignCrew_ShouldReturnCreatedResponse() {
        when(missionAssignmentService.assignCrew(eq(101L), any(MissionAssignmentRequestDTO.class)))
                .thenReturn(List.of(testAssignmentResponse));

        ResponseEntity<List<MissionAssignmentResponseDTO>> response =
                missionAssignmentController.assignCrew(101L, testAssignmentRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(AssignmentRole.PILOT, response.getBody().get(0).assignmentRole());
        verify(missionAssignmentService, times(1))
                .assignCrew(eq(101L), any(MissionAssignmentRequestDTO.class));
    }

    @Test
    void removeAssignment_ShouldReturnNoContent() {
        doNothing().when(missionAssignmentService).removeAssignment(1L);

        ResponseEntity<Void> response = missionAssignmentController.removeAssignment(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(missionAssignmentService, times(1)).removeAssignment(1L);
    }

    @Test
    void assignCrew_WithInvalidRequest_ShouldThrowException() {
        when(missionAssignmentService.assignCrew(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("Invalid crew assignment"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> missionAssignmentController.assignCrew(101L, testAssignmentRequest)
        );

        assertEquals("Invalid crew assignment", ex.getMessage());
        verify(missionAssignmentService, times(1)).assignCrew(anyLong(), any());
    }
}
