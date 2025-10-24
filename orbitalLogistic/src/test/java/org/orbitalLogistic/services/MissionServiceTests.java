package org.orbitalLogistic.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.MissionRequestDTO;
import org.orbitalLogistic.dto.response.MissionResponseDTO;
import org.orbitalLogistic.entities.Mission;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.enums.MissionPriority;
import org.orbitalLogistic.entities.enums.MissionStatus;
import org.orbitalLogistic.entities.enums.MissionType;
import org.orbitalLogistic.exceptions.MissionAlreadyExistsException;
import org.orbitalLogistic.exceptions.MissionNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.MissionMapper;
import org.orbitalLogistic.repositories.MissionRepository;
import org.orbitalLogistic.repositories.SpacecraftRepository;
import org.orbitalLogistic.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Добавляем эту аннотацию
class MissionServiceTests {

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpacecraftRepository spacecraftRepository;

    @Mock
    private MissionMapper missionMapper;

    @InjectMocks
    private MissionService missionService;

    private Mission testMission;
    private User testCommandingOfficer;
    private Spacecraft testSpacecraft;
    private MissionResponseDTO testResponseDTO;
    private MissionRequestDTO testRequestDTO;

    @BeforeEach
    void setUp() {
        testCommandingOfficer = User.builder()
                .id(1L)
                .email("john.doe@example.com")
                .username("John Doe")
                .roleId(1L)
                .passwordHash("encodedPassword")
                .build();

        testSpacecraft = Spacecraft.builder()
                .id(1L)
                .name("Enterprise")
                .registryCode("NCC-1701")
                .build();

        testMission = Mission.builder()
                .id(1L)
                .missionCode("MISSION-001")
                .missionName("Exploration Mission")
                .missionType(MissionType.SCIENCE_EXPEDITION)
                .priority(MissionPriority.HIGH)
                .status(MissionStatus.PLANNING)
                .commandingOfficerId(1L)
                .spacecraftId(1L)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(30))
                .build();

        testResponseDTO = new MissionResponseDTO(
                1L, "MISSION-001", "Exploration Mission", MissionType.SCIENCE_EXPEDITION,
                MissionStatus.PLANNING, MissionPriority.HIGH, "John Doe", "Enterprise",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(30)
        );

        testRequestDTO = new MissionRequestDTO(
                "MISSION-001", "Exploration Mission", MissionType.SCIENCE_EXPEDITION,
                MissionPriority.HIGH, 1L, 1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(30),
                null, null, null, null, null, null
        );

        // Общие заглушки для всех тестов
        setupCommonMocks();
    }

    private void setupCommonMocks() {
        // Общие заглушки для missionMapper
        lenient().when(missionMapper.toResponseDTO(any(Mission.class), anyString(), anyString()))
                .thenReturn(testResponseDTO);

        // Общие заглушки для репозиториев
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(testCommandingOfficer));
        lenient().when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        lenient().when(missionRepository.save(any(Mission.class))).thenReturn(testMission);
    }

    @Test
    void getMissions_WithValidFilters_ShouldReturnPageResponse() {
        // Given
        List<Mission> missions = List.of(testMission);
        when(missionRepository.findWithFilters("MISSION-001", null, "PLANNING", "SCIENCE_EXPEDITION", null, null, null, 20, 0))
                .thenReturn(missions);
        when(missionRepository.countWithFilters("MISSION-001", null, "PLANNING", "SCIENCE_EXPEDITION", null, null, null))
                .thenReturn(1L);

        // When
        PageResponseDTO<MissionResponseDTO> result = missionService.getMissions("MISSION-001", "PLANNING", "SCIENCE_EXPEDITION", 0, 20);

        // Then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(20, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.first());
        assertTrue(result.last());
        verify(missionRepository, times(1)).findWithFilters("MISSION-001", null, "PLANNING", "SCIENCE_EXPEDITION", null, null, null, 20, 0);
        verify(missionRepository, times(1)).countWithFilters("MISSION-001", null, "PLANNING", "SCIENCE_EXPEDITION", null, null, null);
    }

    @Test
    void getMissions_WithNullFilters_ShouldReturnAllMissions() {
        // Given
        List<Mission> missions = List.of(testMission);
        when(missionRepository.findWithFilters(null, null, null, null, null, null, null, 20, 0))
                .thenReturn(missions);
        when(missionRepository.countWithFilters(null, null, null, null, null, null, null))
                .thenReturn(1L);

        // When
        PageResponseDTO<MissionResponseDTO> result = missionService.getMissions(null, null, null, 0, 20);

        // Then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(missionRepository, times(1)).findWithFilters(null, null, null, null, null, null, null, 20, 0);
    }

    @Test
    void getMissionById_WithValidId_ShouldReturnMission() {
        // Given
        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));

        // When
        MissionResponseDTO result = missionService.getMissionById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("MISSION-001", result.missionCode());
        assertEquals("Exploration Mission", result.missionName());
        verify(missionRepository, times(1)).findById(1L);
    }

    @Test
    void getMissionById_WithInvalidId_ShouldThrowException() {
        // Given
        when(missionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        MissionNotFoundException exception = assertThrows(
                MissionNotFoundException.class,
                () -> missionService.getMissionById(999L)
        );

        assertEquals("Mission not found with id: 999", exception.getMessage());
        verify(missionRepository, times(1)).findById(999L);
    }

    @Test
    void getActiveMissions_ShouldReturnActiveMissions() {
        // Given
        List<Mission> activeMissions = List.of(testMission);
        when(missionRepository.findActiveMissions(any(LocalDateTime.class))).thenReturn(activeMissions);

        // When
        List<MissionResponseDTO> result = missionService.getActiveMissions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MISSION-001", result.get(0).missionCode());
        verify(missionRepository, times(1)).findActiveMissions(any(LocalDateTime.class));
    }

    @Test
    void createMission_WithExistingMissionCode_ShouldThrowException() {
        // Given
        when(missionRepository.existsByMissionCode("MISSION-001")).thenReturn(true);

        // When & Then
        MissionAlreadyExistsException exception = assertThrows(
                MissionAlreadyExistsException.class,
                () -> missionService.createMission(testRequestDTO)
        );

        assertEquals("Mission with code already exists: MISSION-001", exception.getMessage());
        verify(missionRepository, times(1)).existsByMissionCode("MISSION-001");
        verify(missionRepository, never()).save(any());
    }

    @Test
    void createMission_WithInvalidCommandingOfficer_ShouldThrowException() {
        // Given
        when(missionRepository.existsByMissionCode("MISSION-001")).thenReturn(false);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        MissionRequestDTO requestWithInvalidOfficer = new MissionRequestDTO(
                "MISSION-001", "Exploration Mission", MissionType.SCIENCE_EXPEDITION,
                MissionPriority.HIGH, 999L, 1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(30),
                null, null, null, null, null, null
        );

        // When & Then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> missionService.createMission(requestWithInvalidOfficer)
        );

        assertEquals("Commanding officer not found", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(missionRepository, never()).save(any());
    }

    @Test
    void createMission_WithInvalidSpacecraft_ShouldThrowException() {
        // Given
        when(missionRepository.existsByMissionCode("MISSION-001")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCommandingOfficer));
        when(spacecraftRepository.findById(999L)).thenReturn(Optional.empty());

        MissionRequestDTO requestWithInvalidSpacecraft = new MissionRequestDTO(
                "MISSION-001", "Exploration Mission", MissionType.SCIENCE_EXPEDITION,
                MissionPriority.HIGH, 1L, 999L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(30),
                null, null, null, null, null, null
        );

        // When & Then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> missionService.createMission(requestWithInvalidSpacecraft)
        );

        assertEquals("Spacecraft not found", exception.getMessage());
        verify(spacecraftRepository, times(1)).findById(999L);
        verify(missionRepository, never()).save(any());
    }

    @Test
    void updateMission_WithValidId_ShouldUpdateMission() {
        // Given
        MissionRequestDTO updateRequest = new MissionRequestDTO(
                "MISSION-001-UPDATED", "Updated Mission", MissionType.CARGO_TRANSPORT,
                MissionPriority.HIGH, 1L, 1L,
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(35),
                null, null, null, null, null, null
        );

        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));
        when(missionRepository.existsByMissionCode("MISSION-001-UPDATED")).thenReturn(false);

        // When
        MissionResponseDTO result = missionService.updateMission(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(missionRepository, times(1)).findById(1L);
        verify(missionRepository, times(1)).existsByMissionCode("MISSION-001-UPDATED");
        verify(missionRepository, times(1)).save(any(Mission.class));
    }

    @Test
    void updateMission_WithInvalidId_ShouldThrowException() {
        // Given
        when(missionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        MissionNotFoundException exception = assertThrows(
                MissionNotFoundException.class,
                () -> missionService.updateMission(999L, testRequestDTO)
        );

        assertEquals("Mission not found with id: 999", exception.getMessage());
        verify(missionRepository, times(1)).findById(999L);
        verify(missionRepository, never()).save(any());
    }

    @Test
    void updateMission_WithExistingMissionCode_ShouldThrowException() {
        // Given
        MissionRequestDTO updateRequest = new MissionRequestDTO(
                "MISSION-002", "Updated Mission", MissionType.CARGO_TRANSPORT,
                MissionPriority.HIGH, 1L, 1L,
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(35),
                null, null, null, null, null, null
        );

        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));
        when(missionRepository.existsByMissionCode("MISSION-002")).thenReturn(true);

        // When & Then
        MissionAlreadyExistsException exception = assertThrows(
                MissionAlreadyExistsException.class,
                () -> missionService.updateMission(1L, updateRequest)
        );

        assertEquals("Mission with code already exists: MISSION-002", exception.getMessage());
        verify(missionRepository, times(1)).existsByMissionCode("MISSION-002");
        verify(missionRepository, never()).save(any());
    }

    @Test
    void updateMission_WithSameMissionCode_ShouldNotThrowException() {
        // Given
        MissionRequestDTO updateRequest = new MissionRequestDTO(
                "MISSION-001", "Updated Mission", MissionType.CARGO_TRANSPORT,
                MissionPriority.HIGH, 1L, 1L,
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(35),
                null, null, null, null, null, null
        );

        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));

        // When
        MissionResponseDTO result = missionService.updateMission(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(missionRepository, never()).existsByMissionCode(anyString());
        verify(missionRepository, times(1)).save(any(Mission.class));
    }

    @Test
    void toResponseDTO_WithInvalidCommandingOfficer_ShouldThrowException() {
        // Given
        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> missionService.getMissionById(1L)
        );

        assertEquals("Commanding officer not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void toResponseDTO_WithInvalidSpacecraft_ShouldThrowException() {
        // Given
        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCommandingOfficer));
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> missionService.getMissionById(1L)
        );

        assertEquals("Spacecraft not found", exception.getMessage());
        verify(spacecraftRepository, times(1)).findById(1L);
    }

    @Test
    void getMissions_WithPagination_ShouldReturnCorrectPage() {
        // Given
        List<Mission> missions = List.of(testMission);
        when(missionRepository.findWithFilters(null, null, null, null, null, null, null, 10, 10))
                .thenReturn(missions);
        when(missionRepository.countWithFilters(null, null, null, null, null, null, null))
                .thenReturn(25L);

        // When
        PageResponseDTO<MissionResponseDTO> result = missionService.getMissions(null, null, null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.currentPage());
        assertEquals(10, result.pageSize());
        assertEquals(25, result.totalElements());
        assertEquals(3, result.totalPages()); // ceil(25/10) = 3
    }
}
