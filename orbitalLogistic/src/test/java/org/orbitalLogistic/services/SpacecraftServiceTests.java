package org.orbitalLogistic.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftResponseDTO;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.SpacecraftType;
import org.orbitalLogistic.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.exceptions.SpacecraftAlreadyExistsException;
import org.orbitalLogistic.exceptions.SpacecraftNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.SpacecraftMapper;
import org.orbitalLogistic.repositories.SpacecraftRepository;
import org.orbitalLogistic.repositories.SpacecraftTypeRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacecraftServiceTests {

    @Mock
    private SpacecraftRepository spacecraftRepository;

    @Mock
    private SpacecraftTypeRepository spacecraftTypeRepository;

    @Mock
    private SpacecraftMapper spacecraftMapper;

    @InjectMocks
    private SpacecraftService spacecraftService;

    private Spacecraft testSpacecraft;
    private SpacecraftType testSpacecraftType;
    private SpacecraftResponseDTO testResponseDTO;
    private SpacecraftRequestDTO testRequestDTO;

    @BeforeEach
    void setUp() {
        testSpacecraftType = SpacecraftType.builder()
                .id(1L)
                .typeName("Cargo Ship")
                .classification(SpacecraftClassification.CARGO_HAULER)
                .build();

        testSpacecraft = Spacecraft.builder()
                .id(1L)
                .registryCode("NCC-1701")
                .name("Enterprise")
                .spacecraftTypeId(1L)
                .massCapacity(BigDecimal.valueOf(1000.0))
                .volumeCapacity(BigDecimal.valueOf(500.0))
                .status(SpacecraftStatus.DOCKED) // Исправлено: DOCKED вместо ACTIVE
                .currentLocation("Earth Orbit")
                .build();

        testResponseDTO = new SpacecraftResponseDTO(
                1L, "NCC-1701", "Enterprise", "Cargo Ship", SpacecraftClassification.CARGO_HAULER,
                BigDecimal.valueOf(1000.0), BigDecimal.valueOf(500.0), SpacecraftStatus.DOCKED, "Earth Orbit",
                BigDecimal.ZERO, BigDecimal.ZERO
        );

        testRequestDTO = new SpacecraftRequestDTO(
                "NCC-1701", "Enterprise", 1L, BigDecimal.valueOf(1000.0),
                BigDecimal.valueOf(500.0), SpacecraftStatus.DOCKED, "Earth Orbit" // Исправлено: DOCKED вместо ACTIVE
        );
    }

    @Test
    void getSpacecrafts_WithValidFilters_ShouldReturnPageResponse() {
        // given
        List<Spacecraft> spacecrafts = List.of(testSpacecraft);
        when(spacecraftRepository.findWithFilters("Enterprise", "DOCKED", 20, 0))
                .thenReturn(spacecrafts);
        when(spacecraftRepository.countWithFilters("Enterprise", "DOCKED"))
                .thenReturn(1L);
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), eq("Cargo Ship"),
                eq(SpacecraftClassification.CARGO_HAULER), eq(BigDecimal.ZERO), eq(BigDecimal.ZERO))) // Исправлено: CARGO_HAULER вместо CARGO
                .thenReturn(testResponseDTO);

        // when
        PageResponseDTO<SpacecraftResponseDTO> result = spacecraftService.getSpacecrafts("Enterprise", "DOCKED", 0, 20);

        // then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(20, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.first());
        assertTrue(result.last());
        verify(spacecraftRepository, times(1)).findWithFilters("Enterprise", "DOCKED", 20, 0);
        verify(spacecraftRepository, times(1)).countWithFilters("Enterprise", "DOCKED");
    }

    @Test
    void getSpacecrafts_WithNullFilters_ShouldReturnAllSpacecrafts() {
        // given
        List<Spacecraft> spacecrafts = List.of(testSpacecraft);
        when(spacecraftRepository.findWithFilters(null, null, 20, 0))
                .thenReturn(spacecrafts);
        when(spacecraftRepository.countWithFilters(null, null))
                .thenReturn(1L);
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), eq("Cargo Ship"),
                eq(SpacecraftClassification.CARGO_HAULER), eq(BigDecimal.ZERO), eq(BigDecimal.ZERO))) // Исправлено: CARGO_HAULER вместо CARGO
                .thenReturn(testResponseDTO);

        // when
        PageResponseDTO<SpacecraftResponseDTO> result = spacecraftService.getSpacecrafts(null, null, 0, 20);

        // then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(spacecraftRepository, times(1)).findWithFilters(null, null, 20, 0);
    }

    @Test
    void getSpacecraftsScroll_WithValidParameters_ShouldReturnList() {
        // given
        List<Spacecraft> spacecrafts = List.of(testSpacecraft);
        when(spacecraftRepository.findWithFilters(null, null, 21, 0))
                .thenReturn(spacecrafts);
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), eq("Cargo Ship"),
                eq(SpacecraftClassification.CARGO_HAULER), eq(BigDecimal.ZERO), eq(BigDecimal.ZERO))) // Исправлено: CARGO_HAULER вместо CARGO
                .thenReturn(testResponseDTO);

        // when
        List<SpacecraftResponseDTO> result = spacecraftService.getSpacecraftsScroll(0, 20);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Enterprise", result.get(0).name());
        verify(spacecraftRepository, times(1)).findWithFilters(null, null, 21, 0);
    }

    @Test
    void getSpacecraftById_WithValidId_ShouldReturnSpacecraft() {
        // given
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), eq("Cargo Ship"),
                eq(SpacecraftClassification.CARGO_HAULER), eq(BigDecimal.ZERO), eq(BigDecimal.ZERO))) // Исправлено: CARGO_HAULER вместо CARGO
                .thenReturn(testResponseDTO);

        // when
        SpacecraftResponseDTO result = spacecraftService.getSpacecraftById(1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Enterprise", result.name());
        verify(spacecraftRepository, times(1)).findById(1L);
    }

    @Test
    void getSpacecraftById_WithInvalidId_ShouldThrowException() {
        // given
        when(spacecraftRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        SpacecraftNotFoundException exception = assertThrows(
                SpacecraftNotFoundException.class,
                () -> spacecraftService.getSpacecraftById(999L)
        );

        assertEquals("Spacecraft not found with id: 999", exception.getMessage());
        verify(spacecraftRepository, times(1)).findById(999L);
    }

    @Test
    void createSpacecraft_WithExistingRegistryCode_ShouldThrowException() {
        // given
        when(spacecraftRepository.existsByRegistryCode("NCC-1701")).thenReturn(true);

        // when & then
        SpacecraftAlreadyExistsException exception = assertThrows(
                SpacecraftAlreadyExistsException.class,
                () -> spacecraftService.createSpacecraft(testRequestDTO)
        );

        assertEquals("Spacecraft with registry code already exists: NCC-1701", exception.getMessage());
        verify(spacecraftRepository, times(1)).existsByRegistryCode("NCC-1701");
        verify(spacecraftRepository, never()).save(any());
    }

    @Test
    void createSpacecraft_WithInvalidTypeId_ShouldThrowException() {
        // given
        when(spacecraftRepository.existsByRegistryCode("NCC-1701")).thenReturn(false);
        when(spacecraftTypeRepository.findById(999L)).thenReturn(Optional.empty());

        SpacecraftRequestDTO requestWithInvalidType = new SpacecraftRequestDTO(
                "NCC-1701", "Enterprise", 999L, BigDecimal.valueOf(1000.0),
                BigDecimal.valueOf(500.0), SpacecraftStatus.DOCKED, "Earth Orbit" // Исправлено: DOCKED вместо ACTIVE
        );

        // when & then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> spacecraftService.createSpacecraft(requestWithInvalidType)
        );

        assertEquals("Spacecraft type not found", exception.getMessage());
        verify(spacecraftTypeRepository, times(1)).findById(999L);
        verify(spacecraftRepository, never()).save(any());
    }

    @Test
    void updateSpacecraft_WithValidId_ShouldUpdateSpacecraft() {
        // given
        SpacecraftRequestDTO updateRequest = new SpacecraftRequestDTO(
                "NCC-1701-A", "Enterprise-A", 1L, BigDecimal.valueOf(1200.0),
                BigDecimal.valueOf(600.0), SpacecraftStatus.MAINTENANCE, "Mars Orbit"
        );

        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftRepository.existsByRegistryCode("NCC-1701-A")).thenReturn(false);
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftRepository.save(any(Spacecraft.class))).thenReturn(testSpacecraft);
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), eq("Cargo Ship"),
                eq(SpacecraftClassification.CARGO_HAULER), eq(BigDecimal.ZERO), eq(BigDecimal.ZERO))) // Исправлено: CARGO_HAULER вместо CARGO
                .thenReturn(testResponseDTO);

        // when
        SpacecraftResponseDTO result = spacecraftService.updateSpacecraft(1L, updateRequest);

        // then
        assertNotNull(result);
        verify(spacecraftRepository, times(1)).findById(1L);
        verify(spacecraftRepository, times(1)).existsByRegistryCode("NCC-1701-A");
        verify(spacecraftRepository, times(1)).save(any(Spacecraft.class));
    }

    @Test
    void updateSpacecraft_WithInvalidId_ShouldThrowException() {
        // given
        when(spacecraftRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        SpacecraftNotFoundException exception = assertThrows(
                SpacecraftNotFoundException.class,
                () -> spacecraftService.updateSpacecraft(999L, testRequestDTO)
        );

        assertEquals("Spacecraft not found with id: 999", exception.getMessage());
        verify(spacecraftRepository, times(1)).findById(999L);
        verify(spacecraftRepository, never()).save(any());
    }

    @Test
    void updateSpacecraft_WithExistingRegistryCode_ShouldThrowException() {
        // given
        SpacecraftRequestDTO updateRequest = new SpacecraftRequestDTO(
                "NCC-1702", "Enterprise", 1L, BigDecimal.valueOf(1000.0),
                BigDecimal.valueOf(500.0), SpacecraftStatus.DOCKED, "Earth Orbit" // Исправлено: DOCKED вместо ACTIVE
        );

        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftRepository.existsByRegistryCode("NCC-1702")).thenReturn(true);

        // when & then
        SpacecraftAlreadyExistsException exception = assertThrows(
                SpacecraftAlreadyExistsException.class,
                () -> spacecraftService.updateSpacecraft(1L, updateRequest)
        );

        assertEquals("Spacecraft with registry code already exists: NCC-1702", exception.getMessage());
        verify(spacecraftRepository, times(1)).existsByRegistryCode("NCC-1702");
        verify(spacecraftRepository, never()).save(any());
    }

    @Test
    void updateSpacecraft_WithSameRegistryCode_ShouldNotThrowException() {
        // given
        SpacecraftRequestDTO updateRequest = new SpacecraftRequestDTO(
                "NCC-1701", "Enterprise Updated", 1L, BigDecimal.valueOf(1200.0),
                BigDecimal.valueOf(600.0), SpacecraftStatus.MAINTENANCE, "Mars Orbit"
        );

        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftRepository.save(any(Spacecraft.class))).thenReturn(testSpacecraft);
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), eq("Cargo Ship"),
                eq(SpacecraftClassification.CARGO_HAULER), eq(BigDecimal.ZERO), eq(BigDecimal.ZERO))) // Исправлено: CARGO_HAULER вместо CARGO
                .thenReturn(testResponseDTO);

        // when
        SpacecraftResponseDTO result = spacecraftService.updateSpacecraft(1L, updateRequest);

        // then
        assertNotNull(result);
        verify(spacecraftRepository, never()).existsByRegistryCode(anyString());
        verify(spacecraftRepository, times(1)).save(any(Spacecraft.class));
    }

    @Test
    void updateSpacecraft_WithNullStatus_ShouldKeepExistingStatus() {
        // given
        SpacecraftRequestDTO updateRequest = new SpacecraftRequestDTO(
                "NCC-1701", "Enterprise Updated", 1L, BigDecimal.valueOf(1200.0),
                BigDecimal.valueOf(600.0), null, "Mars Orbit"
        );

        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftRepository.save(any(Spacecraft.class))).thenReturn(testSpacecraft);
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), eq("Cargo Ship"),
                eq(SpacecraftClassification.CARGO_HAULER), eq(BigDecimal.ZERO), eq(BigDecimal.ZERO))) // Исправлено: CARGO_HAULER вместо CARGO
                .thenReturn(testResponseDTO);

        // when
        SpacecraftResponseDTO result = spacecraftService.updateSpacecraft(1L, updateRequest);

        // then
        assertNotNull(result);
        verify(spacecraftRepository, times(1)).save(any(Spacecraft.class));
    }

    @Test
    void deleteSpacecraft_WithValidId_ShouldDeleteSpacecraft() {
        // given
        when(spacecraftRepository.existsById(1L)).thenReturn(true);

        // when
        spacecraftService.deleteSpacecraft(1L);

        // then
        verify(spacecraftRepository, times(1)).existsById(1L);
        verify(spacecraftRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteSpacecraft_WithInvalidId_ShouldThrowException() {
        // given
        when(spacecraftRepository.existsById(999L)).thenReturn(false);

        // when & then
        SpacecraftNotFoundException exception = assertThrows(
                SpacecraftNotFoundException.class,
                () -> spacecraftService.deleteSpacecraft(999L)
        );

        assertEquals("Spacecraft not found with id: 999", exception.getMessage());
        verify(spacecraftRepository, times(1)).existsById(999L);
        verify(spacecraftRepository, never()).deleteById(any());
    }

    @Test
    void getAvailableSpacecrafts_ShouldReturnAvailableSpacecrafts() {
        // given
        List<Spacecraft> availableSpacecrafts = List.of(testSpacecraft);
        when(spacecraftRepository.findAvailableForMission()).thenReturn(availableSpacecrafts);
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), eq("Cargo Ship"),
                eq(SpacecraftClassification.CARGO_HAULER), eq(BigDecimal.ZERO), eq(BigDecimal.ZERO))) // Исправлено: CARGO_HAULER вместо CARGO
                .thenReturn(testResponseDTO);

        // when
        List<SpacecraftResponseDTO> result = spacecraftService.getAvailableSpacecrafts();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Enterprise", result.get(0).name());
        verify(spacecraftRepository, times(1)).findAvailableForMission();
    }

    @Test
    void updateSpacecraftStatus_WithValidId_ShouldUpdateStatus() {
        // given
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftRepository.save(any(Spacecraft.class))).thenReturn(testSpacecraft);
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), eq("Cargo Ship"),
                eq(SpacecraftClassification.CARGO_HAULER), eq(BigDecimal.ZERO), eq(BigDecimal.ZERO))) // Исправлено: CARGO_HAULER вместо CARGO
                .thenReturn(testResponseDTO);

        // when
        SpacecraftResponseDTO result = spacecraftService.updateSpacecraftStatus(1L, SpacecraftStatus.MAINTENANCE);

        // then
        assertNotNull(result);
        verify(spacecraftRepository, times(1)).findById(1L);
        verify(spacecraftRepository, times(1)).save(any(Spacecraft.class));
    }

    @Test
    void updateSpacecraftStatus_WithInvalidId_ShouldThrowException() {
        // given
        when(spacecraftRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        SpacecraftNotFoundException exception = assertThrows(
                SpacecraftNotFoundException.class,
                () -> spacecraftService.updateSpacecraftStatus(999L, SpacecraftStatus.MAINTENANCE)
        );

        assertEquals("Spacecraft not found with id: 999", exception.getMessage());
        verify(spacecraftRepository, times(1)).findById(999L);
        verify(spacecraftRepository, never()).save(any());
    }

    @Test
    void toResponseDTO_WithValidSpacecraft_ShouldReturnResponseDTO() {
        // given
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), eq("Cargo Ship"),
                eq(SpacecraftClassification.CARGO_HAULER), eq(BigDecimal.ZERO), eq(BigDecimal.ZERO))) // Исправлено: CARGO_HAULER вместо CARGO
                .thenReturn(testResponseDTO);

        // when
        SpacecraftResponseDTO result = spacecraftService.getSpacecraftById(1L);

        // then
        assertNotNull(result);
        assertEquals("Cargo Ship", result.spacecraftTypeName()); // Исправлено: spacecraftTypeName вместо typeName
        assertEquals(SpacecraftClassification.CARGO_HAULER, result.classification()); // Исправлено: CARGO_HAULER вместо CARGO
        verify(spacecraftTypeRepository, times(1)).findById(1L);
    }

    @Test
    void toResponseDTO_WithInvalidType_ShouldThrowException() {
        // given
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> spacecraftService.getSpacecraftById(1L)
        );

        assertEquals("Spacecraft type not found", exception.getMessage());
        verify(spacecraftTypeRepository, times(1)).findById(1L);
    }
}
