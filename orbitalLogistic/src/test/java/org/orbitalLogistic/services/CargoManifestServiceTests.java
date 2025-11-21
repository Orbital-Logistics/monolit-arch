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
import org.orbitalLogistic.dto.request.CargoManifestRequestDTO;
import org.orbitalLogistic.dto.response.CargoManifestResponseDTO;
import org.orbitalLogistic.entities.CargoManifest;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.Cargo;
import org.orbitalLogistic.entities.StorageUnit;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.enums.ManifestStatus;
import org.orbitalLogistic.entities.enums.ManifestPriority;
import org.orbitalLogistic.exceptions.CargoManifestNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.CargoManifestMapper;
import org.orbitalLogistic.repositories.CargoManifestRepository;
import org.orbitalLogistic.repositories.SpacecraftRepository;
import org.orbitalLogistic.repositories.CargoRepository;
import org.orbitalLogistic.repositories.StorageUnitRepository;
import org.orbitalLogistic.repositories.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.orbitalLogistic.dto.response.SpacecraftResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CargoManifestServiceTests {

    @Mock
    private CargoManifestRepository cargoManifestRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SpacecraftService spacecraftService;

    @Mock
    private CargoService cargoService;

    @Mock
    private StorageUnitService storageUnitService;

    @Mock
    private UserService userService;

    @Mock
    private CargoManifestMapper cargoManifestMapper;

    @InjectMocks
    private CargoManifestService cargoManifestService;

    private CargoManifest testManifest;
    private CargoManifestResponseDTO testResponseDTO;
    private CargoManifestRequestDTO testRequestDTO;
    private Spacecraft testSpacecraft;
    private Cargo testCargo;
    private StorageUnit testStorageUnit;
    private User testUser;

    @BeforeEach
    void setUp() {
        testSpacecraft = Spacecraft.builder()
                .id(1L)
                .name("Starship Alpha")
                .build();

        testCargo = Cargo.builder()
                .id(1L)
                .name("Scientific Equipment")
                .build();

        testStorageUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("SU-001")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("john.doe@example.com")
                .username("John Doe")
                .roleId(1L)
                .passwordHash("encodedPassword")
                .build();

        testManifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(100)
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.LOADED)
                .priority(ManifestPriority.HIGH)
                .loadedAt(LocalDateTime.now())
                .build();

        testResponseDTO = new CargoManifestResponseDTO(
                1L, "Starship Alpha", "Scientific Equipment", "SU-001", 100,
                ManifestStatus.LOADED, ManifestPriority.HIGH, LocalDateTime.now(),
                null, "John Doe", null
        );

        testRequestDTO = new CargoManifestRequestDTO(
                1L, 1L, 1L, 100, ManifestPriority.HIGH, 1L,
                null, null, null, null, null, null, null
        );

        cargoManifestService.setSpacecraftService(spacecraftService);
        cargoManifestService.setCargoService(cargoService);
        cargoManifestService.setStorageUnitService(storageUnitService);
        cargoManifestService.setUserService(userService);
    }

    @Test
    void getAllManifests_ShouldReturnPageResponse() {

        List<CargoManifest> manifests = List.of(testManifest);
        when(cargoManifestRepository.count()).thenReturn(1L);
        when(cargoManifestRepository.findAll()).thenReturn(manifests);

        setupCommonMocks();
        when(cargoManifestMapper.toResponseDTO(any(), any(), any(), any(), any(), any()))
                .thenReturn(testResponseDTO);


        PageResponseDTO<CargoManifestResponseDTO> result = cargoManifestService.getAllManifests(0, 10);


        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(10, result.pageSize());
        assertEquals(1, result.totalElements());
    }

    @Test
    void getAllManifests_WhenNoManifests_ShouldReturnEmptyPage() {

        when(cargoManifestRepository.count()).thenReturn(0L);
        when(cargoManifestRepository.findAll()).thenReturn(List.of());


        PageResponseDTO<CargoManifestResponseDTO> result = cargoManifestService.getAllManifests(0, 10);


        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }

    @Test
    void getManifestById_WithValidId_ShouldReturnManifest() {

        when(cargoManifestRepository.findById(1L)).thenReturn(Optional.of(testManifest));
        setupCommonMocks();
        when(cargoManifestMapper.toResponseDTO(any(), any(), any(), any(), any(), any()))
                .thenReturn(testResponseDTO);


        CargoManifestResponseDTO result = cargoManifestService.getManifestById(1L);


        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(cargoManifestRepository, times(1)).findById(1L);
    }

    @Test
    void getManifestById_WithInvalidId_ShouldThrowException() {

        when(cargoManifestRepository.findById(999L)).thenReturn(Optional.empty());


        assertThrows(CargoManifestNotFoundException.class,
                () -> cargoManifestService.getManifestById(999L));

        verify(cargoManifestRepository, times(1)).findById(999L);
    }

    @Test
    void getSpacecraftManifest_ShouldReturnSpacecraftManifests() {

        List<CargoManifest> manifests = List.of(testManifest);
        when(cargoManifestRepository.findBySpacecraftIdOrderByPriorityAndLoadedAt(1L)).thenReturn(manifests);
        setupCommonMocks();
        when(cargoManifestMapper.toResponseDTO(any(), any(), any(), any(), any(), any()))
                .thenReturn(testResponseDTO);


        PageResponseDTO<CargoManifestResponseDTO> result = cargoManifestService.getSpacecraftManifest(1L, 0, 10);


        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(cargoManifestRepository, times(1)).findBySpacecraftIdOrderByPriorityAndLoadedAt(1L);
    }

    @Test
    void loadCargoToSpacecraft_WithSingleCargo_ShouldCreateManifest() {
        when(spacecraftService.getSpacecraftById(1L)).thenReturn(
                new SpacecraftResponseDTO(1L, null, "Starship Alpha", null, null, null, null, null, null, null, null)
        );
        when(cargoService.getCargoById(1L)).thenReturn(
                new org.orbitalLogistic.dto.response.CargoResponseDTO(
                        1L, "Scientific Equipment", "Electronics",
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        org.orbitalLogistic.entities.enums.CargoType.SCIENTIFIC,
                        org.orbitalLogistic.entities.enums.HazardLevel.LOW,
                        0
                )
        );
        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(storageUnitService.getStorageUnitById(1L)).thenReturn(
                new org.orbitalLogistic.dto.response.StorageUnitResponseDTO(
                        1L, "SU-001", "Warehouse A", null,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        0.0, 0.0
                )
        );
        when(storageUnitService.getEntityById(1L)).thenReturn(testStorageUnit);
        when(userService.findUserById(1L)).thenReturn(new org.orbitalLogistic.dto.response.UserResponseDTO(1L, "john.doe@example.com", "John Doe"));
        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(cargoManifestMapper.toEntity(testRequestDTO)).thenReturn(testManifest);
        when(cargoManifestRepository.save(testManifest)).thenReturn(testManifest);

        setupCommonMocks();
        when(cargoManifestMapper.toResponseDTO(any(), any(), any(), any(), any(), any()))
                .thenReturn(testResponseDTO);


        List<CargoManifestResponseDTO> result = cargoManifestService.loadCargoToSpacecraft(1L, testRequestDTO);


        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cargoManifestRepository, times(1)).save(testManifest);
    }

    @Test
    void loadCargoToSpacecraft_WithMultipleCargoItems_ShouldCreateMultipleManifests() {

        CargoManifestRequestDTO.CargoItemDTO item1 = new CargoManifestRequestDTO.CargoItemDTO(1L, 1L, 100, ManifestPriority.HIGH);
        CargoManifestRequestDTO.CargoItemDTO item2 = new CargoManifestRequestDTO.CargoItemDTO(2L, 2L, 50, ManifestPriority.NORMAL);

        CargoManifestRequestDTO multiItemRequest = new CargoManifestRequestDTO(
                1L, null, null, null, null, 1L,
                List.of(item1, item2), null, null, null, null, null, null
        );

        Cargo cargo2 = Cargo.builder().id(2L).name("Food Supplies").build();
        StorageUnit storageUnit2 = StorageUnit.builder().id(2L).unitCode("SU-002").build();

        when(spacecraftService.getSpacecraftById(1L)).thenReturn(
                new SpacecraftResponseDTO(1L, null, "Starship Alpha", null, null, null, null, null, null, null, null)
        );
        when(cargoService.getCargoById(1L)).thenReturn(
                new org.orbitalLogistic.dto.response.CargoResponseDTO(
                        1L, "Scientific Equipment", "Electronics",
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        org.orbitalLogistic.entities.enums.CargoType.SCIENTIFIC,
                        org.orbitalLogistic.entities.enums.HazardLevel.LOW,
                        0
                )
        );
        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(cargoService.getCargoById(2L)).thenReturn(
                new org.orbitalLogistic.dto.response.CargoResponseDTO(
                        2L, "Food Supplies", "Electronics",
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        org.orbitalLogistic.entities.enums.CargoType.SCIENTIFIC,
                        org.orbitalLogistic.entities.enums.HazardLevel.LOW,
                        0
                )
        );
        when(cargoService.getEntityById(2L)).thenReturn(cargo2);
        when(storageUnitService.getStorageUnitById(1L)).thenReturn(
                new org.orbitalLogistic.dto.response.StorageUnitResponseDTO(
                        1L, "SU-001", "Warehouse A", null,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        0.0, 0.0
                )
        );
        when(storageUnitService.getEntityById(1L)).thenReturn(testStorageUnit);
        when(storageUnitService.getStorageUnitById(2L)).thenReturn(
                new org.orbitalLogistic.dto.response.StorageUnitResponseDTO(
                        2L, "SU-002", "Warehouse B", null,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        0.0, 0.0
                )
        );
        when(storageUnitService.getEntityById(2L)).thenReturn(storageUnit2);
        when(userService.findUserById(any())).thenReturn(new org.orbitalLogistic.dto.response.UserResponseDTO(1L, "john.doe@example.com", "John Doe"));
        when(userService.getEntityById(any())).thenReturn(testUser);

        CargoManifest manifest1 = CargoManifest.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .loadedByUserId(1L)
                .quantity(100)
                .priority(ManifestPriority.HIGH)
                .build();

        CargoManifest manifest2 = CargoManifest.builder()
                .id(2L)
                .spacecraftId(1L)
                .cargoId(2L)
                .storageUnitId(2L)
                .loadedByUserId(1L)
                .quantity(50)
                .priority(ManifestPriority.NORMAL)
                .build();

        when(cargoManifestRepository.save(any(CargoManifest.class)))
                .thenReturn(manifest1, manifest2);

        when(cargoManifestMapper.toResponseDTO(any(), any(), any(), any(), any(), any()))
                .thenReturn(testResponseDTO)
                .thenReturn(testResponseDTO);

        when(spacecraftService.getEntityById(1L)).thenReturn(testSpacecraft);


        List<CargoManifestResponseDTO> result = cargoManifestService.loadCargoToSpacecraft(1L, multiItemRequest);


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cargoManifestRepository, times(2)).save(any(CargoManifest.class));
        verify(userService, atLeast(2)).getEntityById(any());
    }

    @Test
    void loadCargoToSpacecraft_WithInvalidSpacecraft_ShouldThrowException() {
        CargoManifestRequestDTO invalidSpacecraftRequest = new CargoManifestRequestDTO(
                999L, 1L, 1L, 100, ManifestPriority.HIGH, 1L,
                null, null, null, null, null, null, null
        );
        when(spacecraftService.getSpacecraftById(999L)).thenThrow(new DataNotFoundException("Spacecraft not found"));


        assertThrows(DataNotFoundException.class,
                () -> cargoManifestService.loadCargoToSpacecraft(999L, invalidSpacecraftRequest));

        verify(spacecraftService, times(1)).getSpacecraftById(999L);
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    void loadCargoToSpacecraft_WithInvalidCargo_ShouldThrowException() {

        CargoManifestRequestDTO requestWithInvalidCargo = new CargoManifestRequestDTO(
                1L, 999L, 1L, 100, ManifestPriority.HIGH, 1L,
                null, null, null, null, null, null, null
        );

        when(spacecraftService.getSpacecraftById(1L)).thenReturn(
                new SpacecraftResponseDTO(1L, null, "Starship Alpha", null, null, null, null, null, null, null, null)
        );
        when(cargoService.getEntityById(999L)).thenThrow(new DataNotFoundException("Cargo not found"));


        assertThrows(DataNotFoundException.class,
                () -> cargoManifestService.loadCargoToSpacecraft(1L, requestWithInvalidCargo));

        verify(cargoService, times(1)).getEntityById(999L);
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    void loadCargoToSpacecraft_WithInvalidStorageUnit_ShouldThrowException() {

        CargoManifestRequestDTO requestWithInvalidStorage = new CargoManifestRequestDTO(
                1L, 1L, 999L, 100, ManifestPriority.HIGH, 1L,
                null, null, null, null, null, null, null
        );

        when(spacecraftService.getSpacecraftById(1L)).thenReturn(
                new SpacecraftResponseDTO(1L, null, "Starship Alpha", null, null, null, null, null, null, null, null)
        );
        lenient().when(cargoService.getCargoById(1L)).thenReturn(null);
        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(storageUnitService.getEntityById(999L)).thenThrow(new DataNotFoundException("Storage unit not found"));


        assertThrows(DataNotFoundException.class,
                () -> cargoManifestService.loadCargoToSpacecraft(1L, requestWithInvalidStorage));

        verify(storageUnitService, times(1)).getEntityById(999L);
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    void loadCargoToSpacecraft_WithInvalidUser_ShouldThrowException() {

        CargoManifestRequestDTO requestWithInvalidUser = new CargoManifestRequestDTO(
                1L, 1L, 1L, 100, ManifestPriority.HIGH, 999L,
                null, null, null, null, null, null, null
        );

        when(spacecraftService.getSpacecraftById(1L)).thenReturn(
                new SpacecraftResponseDTO(1L, null, "Starship Alpha", null, null, null, null, null, null, null, null)
        );
        lenient().when(cargoService.getCargoById(1L)).thenReturn(null);
        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        lenient().when(storageUnitService.getStorageUnitById(1L)).thenReturn(null);
        when(storageUnitService.getEntityById(1L)).thenReturn(testStorageUnit);
        when(userService.getEntityById(999L)).thenThrow(new DataNotFoundException("User not found"));


        assertThrows(DataNotFoundException.class,
                () -> cargoManifestService.loadCargoToSpacecraft(1L, requestWithInvalidUser));

        verify(userService, times(1)).getEntityById(999L);
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    void unloadCargoFromSpacecraft_ShouldUpdateAllActiveManifests() {
        CargoManifestRequestDTO unloadRequest = new CargoManifestRequestDTO(
                null, null, null, null, null, null,
                null, null, 2L, null, null, null, null
        );

        User unloadUser = User.builder()
                .id(2L)
                .email("jane.smith@example.com")
                .username("Jane Smith")
                .roleId(1L)
                .passwordHash("encodedPassword")
                .build();

        CargoManifest activeManifest1 = CargoManifest.builder()
                .id(1L).spacecraftId(1L).cargoId(1L).storageUnitId(1L)
                .quantity(100).loadedByUserId(1L).manifestStatus(ManifestStatus.LOADED).build();

        CargoManifest activeManifest2 = CargoManifest.builder()
                .id(2L).spacecraftId(1L).cargoId(2L).storageUnitId(2L)
                .quantity(50).loadedByUserId(1L).manifestStatus(ManifestStatus.LOADED).build();

        List<CargoManifest> activeManifests = List.of(activeManifest1, activeManifest2);

        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        when(spacecraftService.getSpacecraftById(1L)).thenReturn(
                new SpacecraftResponseDTO(1L, null, "Starship Alpha", null, null, null, null, null, null, null, null)
        );
        when(spacecraftService.getEntityById(1L)).thenReturn(testSpacecraft);
        when(cargoManifestRepository.findActiveCargoBySpacecraft(1L)).thenReturn(activeManifests);
        when(userService.getEntityById(2L)).thenReturn(unloadUser);

        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(cargoService.getEntityById(2L)).thenReturn(testCargo);
        when(storageUnitService.getEntityById(1L)).thenReturn(testStorageUnit);
        when(storageUnitService.getEntityById(2L)).thenReturn(testStorageUnit);
        when(userService.getEntityById(1L)).thenReturn(testUser);


        when(cargoManifestMapper.toResponseDTO(
                any(CargoManifest.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(testResponseDTO);

        List<CargoManifestResponseDTO> result = cargoManifestService.unloadCargoFromSpacecraft(1L, unloadRequest);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(cargoManifestRepository, times(1)).findActiveCargoBySpacecraft(1L);
        verify(jdbcTemplate, times(2)).update(anyString(), any(Object[].class));

        verify(cargoService, atLeastOnce()).getEntityById(1L);
        verify(cargoService, atLeastOnce()).getEntityById(2L);
        verify(storageUnitService, atLeastOnce()).getEntityById(1L);
        verify(storageUnitService, atLeastOnce()).getEntityById(2L);
        verify(userService, atLeastOnce()).getEntityById(1L);

    }

    @Test
    void unloadCargoFromSpacecraft_WithNoActiveManifests_ShouldReturnEmptyList() {

        CargoManifestRequestDTO unloadRequest = new CargoManifestRequestDTO(
                null, null, null, null, null, null,
                null, null, 1L, null, null, null, null
        );

        when(spacecraftService.getSpacecraftById(1L)).thenReturn(
                new SpacecraftResponseDTO(1L, null, "Starship Alpha", null, null, null, null, null, null, null, null)
        );
        when(cargoManifestRepository.findActiveCargoBySpacecraft(1L)).thenReturn(List.of());


        List<CargoManifestResponseDTO> result = cargoManifestService.unloadCargoFromSpacecraft(1L, unloadRequest);


        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cargoManifestRepository, never()).save(any());
    }


    private void setupCommonMocks() {
        lenient().when(spacecraftService.getSpacecraftById(1L)).thenReturn(
                new SpacecraftResponseDTO(1L, null, "Starship Alpha", null, null, null, null, null, null, null, null)
        );
        lenient().when(spacecraftService.getEntityById(1L)).thenReturn(testSpacecraft);
        lenient().when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        lenient().when(storageUnitService.getEntityById(1L)).thenReturn(testStorageUnit);
        lenient().when(userService.getEntityById(1L)).thenReturn(testUser);
        lenient().when(userService.getEntityById(isNull())).thenReturn(testUser);
        lenient().when(userService.getEntityById(2L)).thenReturn(
                User.builder()
                        .id(2L)
                        .email("jane.smith@example.com")
                        .username("Jane Smith")
                        .roleId(1L)
                        .passwordHash("encodedPassword")
                        .build()
        );
    }
}
