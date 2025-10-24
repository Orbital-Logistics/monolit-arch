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
import org.orbitalLogistic.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.dto.response.StorageUnitResponseDTO;
import org.orbitalLogistic.entities.StorageUnit;
import org.orbitalLogistic.entities.enums.StorageTypeEnum;
import org.orbitalLogistic.exceptions.StorageUnitAlreadyExistsException;
import org.orbitalLogistic.exceptions.StorageUnitNotFoundException;
import org.orbitalLogistic.mappers.StorageUnitMapper;
import org.orbitalLogistic.repositories.StorageUnitRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StorageUnitServiceTests {

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @Mock
    private StorageUnitMapper storageUnitMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StorageUnitService storageUnitService;

    private StorageUnit testStorageUnit;
    private StorageUnitResponseDTO testResponseDTO;
    private StorageUnitRequestDTO testRequestDTO;

    @BeforeEach
    void setUp() {
        testStorageUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("SU-001")
                .location("Warehouse A")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(BigDecimal.valueOf(1000.0))
                .totalVolumeCapacity(BigDecimal.valueOf(500.0))
                .currentMass(BigDecimal.valueOf(200.0))
                .currentVolume(BigDecimal.valueOf(100.0))
                .build();

        testResponseDTO = new StorageUnitResponseDTO(
                1L, "SU-001", "Warehouse A", StorageTypeEnum.AMBIENT,
                BigDecimal.valueOf(1000.0), BigDecimal.valueOf(500.0),
                BigDecimal.valueOf(200.0), BigDecimal.valueOf(100.0),
                BigDecimal.valueOf(800.0), BigDecimal.valueOf(400.0),
                20.0, 20.0
        );

        testRequestDTO = new StorageUnitRequestDTO(
                "SU-001", "Warehouse A", StorageTypeEnum.AMBIENT,
                BigDecimal.valueOf(1000.0), BigDecimal.valueOf(500.0)
        );


        lenient().when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), any(Object[].class)))
                .thenReturn(BigDecimal.ZERO);
        lenient().when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
    }

    @Test
    void getStorageUnits_WithValidParameters_ShouldReturnPageResponse() {
        List<StorageUnit> storageUnits = List.of(testStorageUnit);
        when(storageUnitRepository.findAllPaged(20, 0)).thenReturn(storageUnits);
        when(storageUnitRepository.countAll()).thenReturn(1L);
        when(storageUnitMapper.toResponseDTO(any(StorageUnit.class), any(BigDecimal.class),
                any(BigDecimal.class), anyDouble(), anyDouble()))
                .thenReturn(testResponseDTO);

        PageResponseDTO<StorageUnitResponseDTO> result = storageUnitService.getStorageUnits(0, 20);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(20, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.first());
        assertTrue(result.last());
        verify(storageUnitRepository, times(1)).findAllPaged(20, 0);
        verify(storageUnitRepository, times(1)).countAll();
    }

    @Test
    void getStorageUnits_WithNoStorageUnits_ShouldReturnEmptyPage() {
        when(storageUnitRepository.findAllPaged(20, 0)).thenReturn(List.of());
        when(storageUnitRepository.countAll()).thenReturn(0L);

        PageResponseDTO<StorageUnitResponseDTO> result = storageUnitService.getStorageUnits(0, 20);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verify(storageUnitRepository, times(1)).findAllPaged(20, 0);
        verify(storageUnitRepository, times(1)).countAll();
    }

    @Test
    void getStorageUnits_WithLargeDataset_ShouldCalculatePaginationCorrectly() {
        List<StorageUnit> storageUnits = List.of(testStorageUnit);
        when(storageUnitRepository.findAllPaged(10, 20)).thenReturn(storageUnits);
        when(storageUnitRepository.countAll()).thenReturn(50L);
        when(storageUnitMapper.toResponseDTO(any(StorageUnit.class), any(BigDecimal.class),
                any(BigDecimal.class), anyDouble(), anyDouble()))
                .thenReturn(testResponseDTO);

        PageResponseDTO<StorageUnitResponseDTO> result = storageUnitService.getStorageUnits(2, 10);

        assertNotNull(result);
        assertEquals(2, result.currentPage());
        assertEquals(10, result.pageSize());
        assertEquals(50, result.totalElements());
        assertEquals(5, result.totalPages());
        assertFalse(result.first());
        assertFalse(result.last());
        verify(storageUnitRepository, times(1)).findAllPaged(10, 20);
    }

    @Test
    void getStorageUnitById_WithValidId_ShouldReturnStorageUnit() {
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitMapper.toResponseDTO(any(StorageUnit.class), any(BigDecimal.class),
                any(BigDecimal.class), anyDouble(), anyDouble()))
                .thenReturn(testResponseDTO);

        StorageUnitResponseDTO result = storageUnitService.getStorageUnitById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("SU-001", result.unitCode());
        assertEquals("Warehouse A", result.location());
        verify(storageUnitRepository, times(1)).findById(1L);
    }

    @Test
    void getStorageUnitById_WithInvalidId_ShouldThrowException() {
        when(storageUnitRepository.findById(999L)).thenReturn(Optional.empty());

        StorageUnitNotFoundException exception = assertThrows(
                StorageUnitNotFoundException.class,
                () -> storageUnitService.getStorageUnitById(999L)
        );

        assertEquals("Storage unit not found with id: 999", exception.getMessage());
        verify(storageUnitRepository, times(1)).findById(999L);
    }

    @Test
    void createStorageUnit_WithValidRequest_ShouldCreateStorageUnit() {

        Long newId = 1L;

        StorageUnit savedStorageUnit = StorageUnit.builder()
                .id(newId)
                .unitCode("SU-001")
                .location("Warehouse A")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(BigDecimal.valueOf(1000.0))
                .totalVolumeCapacity(BigDecimal.valueOf(500.0))
                .currentMass(BigDecimal.ZERO)
                .currentVolume(BigDecimal.ZERO)
                .build();


        when(storageUnitRepository.existsByUnitCode("SU-001")).thenReturn(false);


        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any()))
                .thenReturn(newId);


        when(storageUnitRepository.findById(newId)).thenReturn(Optional.of(savedStorageUnit));

        when(storageUnitMapper.toResponseDTO(any(StorageUnit.class), any(BigDecimal.class),
                any(BigDecimal.class), anyDouble(), anyDouble()))
                .thenReturn(testResponseDTO);


        StorageUnitResponseDTO result = storageUnitService.createStorageUnit(testRequestDTO);


        assertNotNull(result);
        assertEquals("SU-001", result.unitCode());
        assertEquals("Warehouse A", result.location());


        verify(storageUnitRepository, times(1)).existsByUnitCode("SU-001");
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any());
        verify(storageUnitRepository, times(1)).findById(newId);


        verify(storageUnitMapper, never()).toEntity(any());
        verify(storageUnitRepository, never()).save(any());
    }

    @Test
    void createStorageUnit_WithExistingUnitCode_ShouldThrowException() {
        when(storageUnitRepository.existsByUnitCode("SU-001")).thenReturn(true);

        StorageUnitAlreadyExistsException exception = assertThrows(
                StorageUnitAlreadyExistsException.class,
                () -> storageUnitService.createStorageUnit(testRequestDTO)
        );

        assertEquals("Storage unit with code already exists: SU-001", exception.getMessage());
        verify(storageUnitRepository, times(1)).existsByUnitCode("SU-001");
        verify(storageUnitRepository, never()).save(any());
    }


    @Test
    void updateStorageUnit_WithValidId_ShouldUpdateStorageUnit() {
        StorageUnitRequestDTO updateRequest = new StorageUnitRequestDTO(
                "SU-001-UPDATED", "Warehouse B", StorageTypeEnum.REFRIGERATED,
                BigDecimal.valueOf(1200.0), BigDecimal.valueOf(600.0)
        );

        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitRepository.existsByUnitCode("SU-001-UPDATED")).thenReturn(false);
        when(storageUnitMapper.toResponseDTO(any(StorageUnit.class), any(BigDecimal.class),
                any(BigDecimal.class), anyDouble(), anyDouble()))
                .thenReturn(testResponseDTO);

        StorageUnitResponseDTO result = storageUnitService.updateStorageUnit(1L, updateRequest);

        assertNotNull(result);
        verify(storageUnitRepository, times(1)).findById(1L);
        verify(storageUnitRepository, times(1)).existsByUnitCode("SU-001-UPDATED");


    }

    @Test
    void updateStorageUnit_WithInvalidId_ShouldThrowException() {
        when(storageUnitRepository.findById(999L)).thenReturn(Optional.empty());

        StorageUnitNotFoundException exception = assertThrows(
                StorageUnitNotFoundException.class,
                () -> storageUnitService.updateStorageUnit(999L, testRequestDTO)
        );

        assertEquals("Storage unit not found with id: 999", exception.getMessage());
        verify(storageUnitRepository, times(1)).findById(999L);
        verify(storageUnitRepository, never()).save(any());
    }

    @Test
    void updateStorageUnit_WithExistingUnitCode_ShouldThrowException() {
        StorageUnitRequestDTO updateRequest = new StorageUnitRequestDTO(
                "SU-002", "Warehouse A", StorageTypeEnum.AMBIENT,
                BigDecimal.valueOf(1000.0), BigDecimal.valueOf(500.0)
        );

        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitRepository.existsByUnitCode("SU-002")).thenReturn(true);

        StorageUnitAlreadyExistsException exception = assertThrows(
                StorageUnitAlreadyExistsException.class,
                () -> storageUnitService.updateStorageUnit(1L, updateRequest)
        );

        assertEquals("Storage unit with code already exists: SU-002", exception.getMessage());
        verify(storageUnitRepository, times(1)).existsByUnitCode("SU-002");
        verify(storageUnitRepository, never()).save(any());
    }

    @Test
    void updateStorageUnit_WithSameUnitCode_ShouldNotThrowException() {
        StorageUnitRequestDTO updateRequest = new StorageUnitRequestDTO(
                "SU-001", "Warehouse B", StorageTypeEnum.REFRIGERATED,
                BigDecimal.valueOf(1200.0), BigDecimal.valueOf(600.0)
        );

        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitMapper.toResponseDTO(any(StorageUnit.class), any(BigDecimal.class),
                any(BigDecimal.class), anyDouble(), anyDouble()))
                .thenReturn(testResponseDTO);

        StorageUnitResponseDTO result = storageUnitService.updateStorageUnit(1L, updateRequest);

        assertNotNull(result);
        verify(storageUnitRepository, never()).existsByUnitCode(anyString());


    }

    @Test
    void getStorageUnitInventory_ShouldThrowStorageUnitNotFoundException_WhenStorageUnitNotFound() {
        Long nonExistentStorageUnitId = 1L;


        StorageUnitNotFoundException exception = assertThrows(
                StorageUnitNotFoundException.class,
                () -> storageUnitService.getStorageUnitInventory(nonExistentStorageUnitId, 0, 20)
        );

        assertEquals("Storage unit not found with id: " + nonExistentStorageUnitId, exception.getMessage());
    }

    @Test
    void toResponseDTO_WithValidStorageUnit_ShouldCalculateUsageCorrectly() {
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitMapper.toResponseDTO(any(StorageUnit.class), any(BigDecimal.class),
                any(BigDecimal.class), anyDouble(), anyDouble()))
                .thenAnswer(invocation -> {
                    StorageUnit unit = invocation.getArgument(0);
                    BigDecimal availableMass = invocation.getArgument(1);
                    BigDecimal availableVolume = invocation.getArgument(2);
                    Double massUsage = invocation.getArgument(3);
                    Double volumeUsage = invocation.getArgument(4);

                    return new StorageUnitResponseDTO(
                            unit.getId(), unit.getUnitCode(), unit.getLocation(), unit.getStorageType(),
                            unit.getTotalMassCapacity(), unit.getTotalVolumeCapacity(),
                            unit.getCurrentMass(), unit.getCurrentVolume(),
                            availableMass, availableVolume, massUsage, volumeUsage
                    );
                });

        StorageUnitResponseDTO result = storageUnitService.getStorageUnitById(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(800.0), result.availableMassCapacity());
        assertEquals(BigDecimal.valueOf(400.0), result.availableVolumeCapacity());
        assertEquals(20.0, result.massUsagePercentage());
        assertEquals(20.0, result.volumeUsagePercentage());
    }

    @Test
    void toResponseDTO_WithZeroCapacity_ShouldHandleDivisionByZero() {
        StorageUnit zeroCapacityUnit = StorageUnit.builder()
                .id(2L)
                .unitCode("SU-002")
                .location("Empty Warehouse")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(BigDecimal.ZERO)
                .totalVolumeCapacity(BigDecimal.ZERO)
                .currentMass(BigDecimal.ZERO)
                .currentVolume(BigDecimal.ZERO)
                .build();

        when(storageUnitRepository.findById(2L)).thenReturn(Optional.of(zeroCapacityUnit));
        when(storageUnitMapper.toResponseDTO(any(StorageUnit.class), any(BigDecimal.class),
                any(BigDecimal.class), anyDouble(), anyDouble()))
                .thenAnswer(invocation -> {
                    StorageUnit unit = invocation.getArgument(0);
                    BigDecimal availableMass = invocation.getArgument(1);
                    BigDecimal availableVolume = invocation.getArgument(2);
                    Double massUsage = invocation.getArgument(3);
                    Double volumeUsage = invocation.getArgument(4);

                    return new StorageUnitResponseDTO(
                            unit.getId(), unit.getUnitCode(), unit.getLocation(), unit.getStorageType(),
                            unit.getTotalMassCapacity(), unit.getTotalVolumeCapacity(),
                            unit.getCurrentMass(), unit.getCurrentVolume(),
                            availableMass, availableVolume, massUsage, volumeUsage
                    );
                });

        StorageUnitResponseDTO result = storageUnitService.getStorageUnitById(2L);

        assertNotNull(result);
        assertEquals(0.0, result.massUsagePercentage());
        assertEquals(0.0, result.volumeUsagePercentage());
    }
}
