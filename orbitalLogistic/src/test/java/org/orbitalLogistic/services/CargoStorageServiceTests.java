package org.orbitalLogistic.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoStorageRequestDTO;
import org.orbitalLogistic.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.entities.Cargo;
import org.orbitalLogistic.entities.CargoStorage;
import org.orbitalLogistic.entities.StorageUnit;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.exceptions.CargoStorageNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.CargoStorageMapper;
import org.orbitalLogistic.repositories.CargoRepository;
import org.orbitalLogistic.repositories.CargoStorageRepository;
import org.orbitalLogistic.repositories.StorageUnitRepository;
import org.orbitalLogistic.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoStorageServiceTests {

    @Mock
    private CargoStorageRepository cargoStorageRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CargoStorageMapper cargoStorageMapper;

    @InjectMocks
    private CargoStorageService cargoStorageService;

    private CargoStorage testCargoStorage;
    private Cargo testCargo;
    private StorageUnit testStorageUnit;
    private User testUser;
    private CargoStorageResponseDTO testResponseDTO;
    private CargoStorageRequestDTO testRequestDTO;

    @BeforeEach
    void setUp() {
        testCargo = Cargo.builder()
                .id(1L)
                .name("Scientific Equipment")
                .build();

        testStorageUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("SU-001")
                .location("Warehouse A")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("john.doe@example.com")
                .username("John Doe")
                .roleId(1L)
                .passwordHash("encodedPassword")
                .build();

        testCargoStorage = CargoStorage.builder()
                .id(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(100)
                .storedAt(LocalDateTime.now())
                .lastInventoryCheck(LocalDateTime.now())
                .lastCheckedByUserId(1L)
                .build();

        testResponseDTO = new CargoStorageResponseDTO(
                1L, "SU-001", "Warehouse A", "Scientific Equipment", 100,
                LocalDateTime.now(), LocalDateTime.now(), "John Doe"
        );

        testRequestDTO = new CargoStorageRequestDTO(
                1L, 1L, 100, 1L, "Transfer", "Moving cargo"
        );
    }

    @Test
    void getAllCargoStorage_WithValidParameters_ShouldReturnPageResponse() {

        List<CargoStorage> cargoStorages = List.of(testCargoStorage);
        when(cargoStorageRepository.count()).thenReturn(1L);
        when(cargoStorageRepository.findAll()).thenReturn(cargoStorages);
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cargoStorageMapper.toResponseDTO(any(CargoStorage.class), eq("SU-001"),
                eq("Warehouse A"), eq("Scientific Equipment"), eq("John Doe")))
                .thenReturn(testResponseDTO);


        PageResponseDTO<CargoStorageResponseDTO> result = cargoStorageService.getAllCargoStorage(0, 20);


        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(20, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        verify(cargoStorageRepository, times(1)).count();
        verify(cargoStorageRepository, times(1)).findAll();
    }

    @Test
    void getAllCargoStorage_WithNoStorage_ShouldReturnEmptyPage() {

        when(cargoStorageRepository.count()).thenReturn(0L);
        when(cargoStorageRepository.findAll()).thenReturn(List.of());


        PageResponseDTO<CargoStorageResponseDTO> result = cargoStorageService.getAllCargoStorage(0, 20);


        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verify(cargoStorageRepository, times(1)).count();
        verify(cargoStorageRepository, times(1)).findAll();
    }

    @Test
    void getStorageUnitCargo_WithValidStorageUnitId_ShouldReturnPageResponse() {

        List<CargoStorage> cargoStorages = List.of(testCargoStorage);
        when(cargoStorageRepository.findByStorageUnitIdOrderByStoredAt(1L)).thenReturn(cargoStorages);
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cargoStorageMapper.toResponseDTO(any(CargoStorage.class), eq("SU-001"),
                eq("Warehouse A"), eq("Scientific Equipment"), eq("John Doe")))
                .thenReturn(testResponseDTO);


        PageResponseDTO<CargoStorageResponseDTO> result = cargoStorageService.getStorageUnitCargo(1L, 0, 20);


        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(20, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        verify(cargoStorageRepository, times(1)).findByStorageUnitIdOrderByStoredAt(1L);
    }

    @Test
    void getStorageUnitCargo_WithNoCargo_ShouldReturnEmptyPage() {

        when(cargoStorageRepository.findByStorageUnitIdOrderByStoredAt(1L)).thenReturn(List.of());


        PageResponseDTO<CargoStorageResponseDTO> result = cargoStorageService.getStorageUnitCargo(1L, 0, 20);


        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verify(cargoStorageRepository, times(1)).findByStorageUnitIdOrderByStoredAt(1L);
    }

    @Test
    void addCargoToStorage_WithNewCargo_ShouldCreateStorage() {

        CargoStorage newCargoStorage = CargoStorage.builder()
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(100)
                .storedAt(LocalDateTime.now())
                .build();

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cargoStorageRepository.findByStorageUnitIdAndCargoId(1L, 1L)).thenReturn(List.of());
        when(cargoStorageMapper.toEntity(testRequestDTO)).thenReturn(newCargoStorage);
        when(cargoStorageRepository.save(newCargoStorage)).thenReturn(testCargoStorage);

        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cargoStorageMapper.toResponseDTO(any(CargoStorage.class), eq("SU-001"),
                eq("Warehouse A"), eq("Scientific Equipment"), eq("John Doe")))
                .thenReturn(testResponseDTO);


        CargoStorageResponseDTO result = cargoStorageService.addCargoToStorage(testRequestDTO);


        assertNotNull(result);
        assertEquals(100, result.quantity());

        verify(cargoRepository, times(2)).findById(1L);
        verify(storageUnitRepository, times(2)).findById(1L);
        verify(userRepository, times(2)).findById(1L);
        verify(cargoStorageRepository, times(1)).findByStorageUnitIdAndCargoId(1L, 1L);
        verify(cargoStorageMapper, times(1)).toEntity(testRequestDTO);
        verify(cargoStorageRepository, times(1)).save(newCargoStorage);
    }

    @Test
    void addCargoToStorage_WithInvalidCargo_ShouldThrowException() {

        when(cargoRepository.findById(999L)).thenReturn(Optional.empty());

        CargoStorageRequestDTO requestWithInvalidCargo = new CargoStorageRequestDTO(
                1L, 999L, 100, 1L, "Transfer", "Moving cargo"
        );


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> cargoStorageService.addCargoToStorage(requestWithInvalidCargo)
        );

        assertEquals("Cargo not found", exception.getMessage());
        verify(cargoRepository, times(1)).findById(999L);
        verify(cargoStorageRepository, never()).save(any());
    }

    @Test
    void addCargoToStorage_WithInvalidStorageUnit_ShouldThrowException() {

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(storageUnitRepository.findById(999L)).thenReturn(Optional.empty());

        CargoStorageRequestDTO requestWithInvalidStorageUnit = new CargoStorageRequestDTO(
                999L, 1L, 100, 1L, "Transfer", "Moving cargo"
        );


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> cargoStorageService.addCargoToStorage(requestWithInvalidStorageUnit)
        );

        assertEquals("Storage unit not found", exception.getMessage());
        verify(storageUnitRepository, times(1)).findById(999L);
        verify(cargoStorageRepository, never()).save(any());
    }

    @Test
    void addCargoToStorage_WithInvalidUser_ShouldThrowException() {

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        CargoStorageRequestDTO requestWithInvalidUser = new CargoStorageRequestDTO(
                1L, 1L, 100, 999L, "Transfer", "Moving cargo"
        );


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> cargoStorageService.addCargoToStorage(requestWithInvalidUser)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(cargoStorageRepository, never()).save(any());
    }

    @Test
    void addCargoToStorage_WithNullUser_ShouldNotValidateUser() {

        CargoStorageRequestDTO requestWithNullUser = new CargoStorageRequestDTO(
                1L, 1L, 100, null, "Transfer", "Moving cargo"
        );

        CargoStorage newCargoStorage = CargoStorage.builder()
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(100)
                .storedAt(LocalDateTime.now())
                .build();

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(cargoStorageRepository.findByStorageUnitIdAndCargoId(1L, 1L)).thenReturn(List.of());
        when(cargoStorageMapper.toEntity(requestWithNullUser)).thenReturn(newCargoStorage);
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(testCargoStorage);

        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(cargoStorageMapper.toResponseDTO(any(CargoStorage.class), eq("SU-001"),
                eq("Warehouse A"), eq("Scientific Equipment"), isNull()))
                .thenReturn(testResponseDTO);


        CargoStorageResponseDTO result = cargoStorageService.addCargoToStorage(requestWithNullUser);


        assertNotNull(result);

        verify(cargoStorageRepository, times(1)).save(any(CargoStorage.class));
    }

    @Test
    void updateQuantity_WithValidId_ShouldUpdateQuantity() {

        CargoStorageRequestDTO updateRequest = new CargoStorageRequestDTO(
                1L, 1L, 200, 1L, "Update", "Updating quantity"
        );


        CargoStorage updatedStorage = CargoStorage.builder()
                .id(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(200)
                .storedAt(LocalDateTime.now())
                .lastInventoryCheck(LocalDateTime.now())
                .lastCheckedByUserId(1L)
                .build();

        when(cargoStorageRepository.findById(1L)).thenReturn(Optional.of(testCargoStorage));
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(updatedStorage);
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cargoStorageMapper.toResponseDTO(any(CargoStorage.class), eq("SU-001"),
                eq("Warehouse A"), eq("Scientific Equipment"), eq("John Doe")))
                .thenReturn(testResponseDTO);


        CargoStorageResponseDTO result = cargoStorageService.updateQuantity(1L, updateRequest);


        assertNotNull(result);
        verify(cargoStorageRepository, times(1)).findById(1L);
        verify(cargoStorageRepository, times(1)).save(any(CargoStorage.class));
    }

    @Test
    void updateQuantity_WithInvalidId_ShouldThrowException() {

        when(cargoStorageRepository.findById(999L)).thenReturn(Optional.empty());


        CargoStorageNotFoundException exception = assertThrows(
                CargoStorageNotFoundException.class,
                () -> cargoStorageService.updateQuantity(999L, testRequestDTO)
        );

        assertEquals("Cargo storage not found with id: 999", exception.getMessage());
        verify(cargoStorageRepository, times(1)).findById(999L);
        verify(cargoStorageRepository, never()).save(any());
    }

    @Test
    void addCargoToStorage_WithExistingCargo_ShouldUpdateQuantity() {

        CargoStorage existingStorage = CargoStorage.builder()
                .id(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(50)
                .build();

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(userRepository.findById(any())).thenReturn(Optional.of(testUser));
        when(cargoStorageRepository.findByStorageUnitIdAndCargoId(1L, 1L)).thenReturn(List.of(existingStorage));
        when(cargoStorageRepository.save(existingStorage)).thenReturn(existingStorage);

        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cargoStorageMapper.toResponseDTO(any(CargoStorage.class), anyString(), anyString(), anyString(), any()))
                .thenReturn(testResponseDTO);


        CargoStorageResponseDTO result = cargoStorageService.addCargoToStorage(testRequestDTO);


        assertNotNull(result);
        assertEquals(150, existingStorage.getQuantity());
        verify(cargoStorageRepository, times(1)).save(existingStorage);
        verify(cargoStorageMapper, never()).toEntity(any());

        verify(cargoRepository, times(2)).findById(1L);
    }

    @Test
    void updateQuantity_WithNullUser_ShouldNotUpdateUser() {

        CargoStorageRequestDTO updateRequest = new CargoStorageRequestDTO(
                1L, 1L, 200, null, "Update", "Updating quantity"
        );


        CargoStorage cargoStorageWithNullUser = CargoStorage.builder()
                .id(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(100)
                .lastCheckedByUserId(null)
                .build();

        when(cargoStorageRepository.findById(1L)).thenReturn(Optional.of(cargoStorageWithNullUser));
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(cargoStorageWithNullUser);
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));

        when(cargoStorageMapper.toResponseDTO(any(CargoStorage.class), eq("SU-001"),
                eq("Warehouse A"), eq("Scientific Equipment"), isNull()))
                .thenReturn(testResponseDTO);


        CargoStorageResponseDTO result = cargoStorageService.updateQuantity(1L, updateRequest);


        assertNotNull(result);
        assertNull(cargoStorageWithNullUser.getLastCheckedByUserId());
        verify(cargoStorageRepository, times(1)).save(cargoStorageWithNullUser);
    }

    @Test
    void toResponseDTO_WithValidCargoStorage_ShouldReturnResponseDTO() {

        CargoStorage savedCargoStorage = CargoStorage.builder()
                .id(1L)
                .storageUnitId(1L)
                .cargoId(1L)
                .quantity(200)
                .lastCheckedByUserId(1L)
                .build();

        when(cargoStorageRepository.findById(1L)).thenReturn(Optional.of(testCargoStorage));
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(savedCargoStorage);
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cargoStorageMapper.toResponseDTO(eq(savedCargoStorage), eq("SU-001"),
                eq("Warehouse A"), eq("Scientific Equipment"), eq("John Doe")))
                .thenReturn(testResponseDTO);


        CargoStorageResponseDTO result = cargoStorageService.updateQuantity(1L, testRequestDTO);


        assertNotNull(result);
        assertEquals("SU-001", result.storageUnitCode());
        assertEquals("Warehouse A", result.storageLocation());
        assertEquals("Scientific Equipment", result.cargoName());
        assertEquals("John Doe", result.lastCheckedByUserName());
        verify(storageUnitRepository, times(1)).findById(1L);
        verify(cargoRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void toResponseDTO_WithInvalidStorageUnit_ShouldThrowException() {

        CargoStorage savedCargoStorage = CargoStorage.builder()
                .id(1L)
                .storageUnitId(1L)
                .cargoId(1L)
                .quantity(200)
                .build();

        when(cargoStorageRepository.findById(1L)).thenReturn(Optional.of(testCargoStorage));
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(savedCargoStorage);
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.empty());


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> cargoStorageService.updateQuantity(1L, testRequestDTO)
        );

        assertEquals("Storage unit not found", exception.getMessage());
        verify(storageUnitRepository, times(1)).findById(1L);
    }

    @Test
    void toResponseDTO_WithInvalidCargo_ShouldThrowException() {

        CargoStorage savedCargoStorage = CargoStorage.builder()
                .id(1L)
                .storageUnitId(1L)
                .cargoId(1L)
                .quantity(200)
                .build();

        when(cargoStorageRepository.findById(1L)).thenReturn(Optional.of(testCargoStorage));
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(savedCargoStorage);
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(cargoRepository.findById(1L)).thenReturn(Optional.empty());


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> cargoStorageService.updateQuantity(1L, testRequestDTO)
        );

        assertEquals("Cargo not found", exception.getMessage());
        verify(cargoRepository, times(1)).findById(1L);
    }

    @Test
    void toResponseDTO_WithNullUser_ShouldReturnNullUserName() {

        CargoStorage savedCargoStorage = CargoStorage.builder()
                .id(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(200)
                .lastCheckedByUserId(null)
                .build();

        when(cargoStorageRepository.findById(1L)).thenReturn(Optional.of(testCargoStorage));
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(savedCargoStorage);
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));

        when(cargoStorageMapper.toResponseDTO(eq(savedCargoStorage), eq("SU-001"),
                eq("Warehouse A"), eq("Scientific Equipment"), isNull()))
                .thenReturn(testResponseDTO);


        CargoStorageResponseDTO result = cargoStorageService.updateQuantity(1L, testRequestDTO);


        assertNotNull(result);

        verify(userRepository, never()).findById(any());
    }
    @Test
    void getAllCargoStorage_WithPagination_ShouldReturnCorrectPage() {

        List<CargoStorage> cargoStorages = List.of(testCargoStorage);
        when(cargoStorageRepository.count()).thenReturn(25L);
        when(cargoStorageRepository.findAll()).thenReturn(cargoStorages);



        lenient().when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        lenient().when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        lenient().when(cargoStorageMapper.toResponseDTO(any(CargoStorage.class), eq("SU-001"),
                        eq("Warehouse A"), eq("Scientific Equipment"), eq("John Doe")))
                .thenReturn(testResponseDTO);


        PageResponseDTO<CargoStorageResponseDTO> result = cargoStorageService.getAllCargoStorage(1, 10);


        assertNotNull(result);
        assertEquals(1, result.currentPage());
        assertEquals(10, result.pageSize());
        assertEquals(25, result.totalElements());
        assertEquals(3, result.totalPages());
    }

    @Test
    void getStorageUnitCargo_WithPagination_ShouldReturnCorrectPage() {

        List<CargoStorage> cargoStorages = List.of(testCargoStorage);
        when(cargoStorageRepository.findByStorageUnitIdOrderByStoredAt(1L)).thenReturn(cargoStorages);


        lenient().when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        lenient().when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        lenient().when(cargoStorageMapper.toResponseDTO(any(CargoStorage.class), eq("SU-001"),
                        eq("Warehouse A"), eq("Scientific Equipment"), eq("John Doe")))
                .thenReturn(testResponseDTO);


        PageResponseDTO<CargoStorageResponseDTO> result = cargoStorageService.getStorageUnitCargo(1L, 1, 5);


        assertNotNull(result);
        assertEquals(1, result.currentPage());
        assertEquals(5, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
    }
}
