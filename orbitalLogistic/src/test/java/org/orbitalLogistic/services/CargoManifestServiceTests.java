package org.orbitalLogistic.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoManifestServiceTests {

    @Mock
    private CargoManifestRepository cargoManifestRepository;

    @Mock
    private SpacecraftRepository spacecraftRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @Mock
    private UserRepository userRepository;

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
                .first_name("John")
                .last_name("Doe")
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
        
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
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
                null, null, null, null, null, 1L,
                List.of(item1, item2), null, null, null, null, null, null
        );

        Cargo cargo2 = Cargo.builder().id(2L).name("Food Supplies").build();
        StorageUnit storageUnit2 = StorageUnit.builder().id(2L).unitCode("SU-002").build();

        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(cargoRepository.findById(2L)).thenReturn(Optional.of(cargo2));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitRepository.findById(2L)).thenReturn(Optional.of(storageUnit2));

        
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));

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

        
        List<CargoManifestResponseDTO> result = cargoManifestService.loadCargoToSpacecraft(1L, multiItemRequest);

        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cargoManifestRepository, times(2)).save(any(CargoManifest.class));
        verify(userRepository, atLeast(2)).findById(any());
    }

    @Test
    void loadCargoToSpacecraft_WithInvalidSpacecraft_ShouldThrowException() {
        
        when(spacecraftRepository.findById(999L)).thenReturn(Optional.empty());

        
        assertThrows(DataNotFoundException.class,
                () -> cargoManifestService.loadCargoToSpacecraft(999L, testRequestDTO));

        verify(spacecraftRepository, times(1)).findById(999L);
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    void loadCargoToSpacecraft_WithInvalidCargo_ShouldThrowException() {
        
        CargoManifestRequestDTO requestWithInvalidCargo = new CargoManifestRequestDTO(
                1L, 999L, 1L, 100, ManifestPriority.HIGH, 1L,
                null, null, null, null, null, null, null
        );

        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(cargoRepository.findById(999L)).thenReturn(Optional.empty());

        
        assertThrows(DataNotFoundException.class,
                () -> cargoManifestService.loadCargoToSpacecraft(1L, requestWithInvalidCargo));

        verify(cargoRepository, times(1)).findById(999L);
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    void unloadCargoFromSpacecraft_ShouldUpdateAllActiveManifests() {
        
        CargoManifestRequestDTO unloadRequest = new CargoManifestRequestDTO(
                null, null, null, null, null, null,
                null, null, 2L, null, null, null, null
        );

        User unloadUser = User.builder().id(2L).first_name("Jane").last_name("Smith").build();

        CargoManifest activeManifest1 = CargoManifest.builder()
                .id(1L).spacecraftId(1L).cargoId(1L).storageUnitId(1L)
                .quantity(100).loadedByUserId(1L).manifestStatus(ManifestStatus.LOADED).build();

        CargoManifest activeManifest2 = CargoManifest.builder()
                .id(2L).spacecraftId(1L).cargoId(1L).storageUnitId(1L)
                .quantity(50).loadedByUserId(1L).manifestStatus(ManifestStatus.LOADED).build();

        List<CargoManifest> activeManifests = List.of(activeManifest1, activeManifest2);

        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(cargoManifestRepository.findActiveCargoBySpacecraft(1L)).thenReturn(activeManifests);
        when(userRepository.findById(2L)).thenReturn(Optional.of(unloadUser));

        when(cargoManifestRepository.save(any(CargoManifest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        setupCommonMocks();
        when(cargoManifestMapper.toResponseDTO(any(), any(), any(), any(), any(), any()))
                .thenReturn(testResponseDTO);

        
        List<CargoManifestResponseDTO> result = cargoManifestService.unloadCargoFromSpacecraft(1L, unloadRequest);

        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cargoManifestRepository, times(2)).save(any(CargoManifest.class));
    }

    @Test
    void unloadCargoFromSpacecraft_WithNoActiveManifests_ShouldReturnEmptyList() {
        
        CargoManifestRequestDTO unloadRequest = new CargoManifestRequestDTO(
                null, null, null, null, null, null,
                null, null, 1L, null, null, null, null
        );

        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(cargoManifestRepository.findActiveCargoBySpacecraft(1L)).thenReturn(List.of());

        
        List<CargoManifestResponseDTO> result = cargoManifestService.unloadCargoFromSpacecraft(1L, unloadRequest);

        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cargoManifestRepository, never()).save(any());
    }

    
    private void setupCommonMocks() {
        lenient().when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        lenient().when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        lenient().when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        lenient().when(userRepository.findById(2L)).thenReturn(Optional.of(
                User.builder().id(2L).first_name("Jane").last_name("Smith").build()
        ));
    }
}
