package org.orbitalLogistic.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.dto.response.InventoryTransactionResponseDTO;
import org.orbitalLogistic.entities.Cargo;
import org.orbitalLogistic.entities.InventoryTransaction;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.StorageUnit;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.enums.TransactionType;
import org.orbitalLogistic.exceptions.InventoryTransactionNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.InventoryTransactionMapper;
import org.orbitalLogistic.repositories.CargoManifestRepository;
import org.orbitalLogistic.repositories.CargoRepository;
import org.orbitalLogistic.repositories.InventoryTransactionRepository;
import org.orbitalLogistic.repositories.SpacecraftRepository;
import org.orbitalLogistic.repositories.StorageUnitRepository;
import org.orbitalLogistic.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryTransactionServiceTests {

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private CargoService cargoService;

    @Mock
    private StorageUnitService storageUnitService;

    @Mock
    private SpacecraftService spacecraftService;

    @Mock
    private UserService userService;

    @Mock
    private InventoryTransactionMapper inventoryTransactionMapper;

    @InjectMocks
    private InventoryTransactionService inventoryTransactionService;

    private InventoryTransaction testTransaction;
    private Cargo testCargo;
    private User testUser;
    private StorageUnit testStorageUnit;
    private Spacecraft testSpacecraft;
    private InventoryTransactionResponseDTO testResponseDTO;
    private InventoryTransactionRequestDTO testRequestDTO;

    @BeforeEach
    void setUp() {
        testCargo = Cargo.builder()
                .id(1L)
                .name("Scientific Equipment")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("john.doe@example.com")
                .username("John Doe")
                .roleId(1L)
                .passwordHash("encodedPassword")
                .build();

        testStorageUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("SU-001")
                .location("Warehouse A")
                .build();

        testSpacecraft = Spacecraft.builder()
                .id(1L)
                .name("Enterprise")
                .registryCode("NCC-1701")
                .build();

        testTransaction = InventoryTransaction.builder()
                .id(1L)
                .cargoId(1L)
                .performedByUserId(1L)
                .fromStorageUnitId(1L)
                .toStorageUnitId(2L)
                .quantity(100)
                .transactionType(TransactionType.TRANSFER)
                .transactionDate(LocalDateTime.now())
                .notes("Transfer between storages")
                .build();

        testResponseDTO = new InventoryTransactionResponseDTO(
                1L, TransactionType.TRANSFER, "Scientific Equipment", 100,
                "Storage: SU-001", "Storage: SU-002", "John Doe",
                LocalDateTime.now(), "TRANSFER", "REF-001", "Transfer between storages"
        );

        testRequestDTO = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 1L, 100, 1L, 2L, null, null, 1L, "TRANSFER", "REF-001", "Transfer between storages"
        );
        inventoryTransactionService.setCargoService(cargoService);
        inventoryTransactionService.setStorageUnitService(storageUnitService);
        inventoryTransactionService.setSpacecraftService(spacecraftService);
        inventoryTransactionService.setUserService(userService);
    }

    @Test
    void getAllTransactions_WithValidParameters_ShouldReturnPageResponse() {

        List<InventoryTransaction> transactions = List.of(testTransaction);
        when(inventoryTransactionRepository.count()).thenReturn(1L);
        when(inventoryTransactionRepository.findAll()).thenReturn(transactions);
        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(storageUnitService.getEntityById(1L)).thenReturn(testStorageUnit);
        when(storageUnitService.getEntityById(2L)).thenReturn(testStorageUnit);

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testResponseDTO);


        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getAllTransactions(0, 20);


        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(20, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        verify(inventoryTransactionRepository, times(1)).count();
        verify(inventoryTransactionRepository, times(1)).findAll();
    }

    @Test
    void getAllTransactions_WithNoTransactions_ShouldReturnEmptyPage() {

        when(inventoryTransactionRepository.count()).thenReturn(0L);
        when(inventoryTransactionRepository.findAll()).thenReturn(List.of());


        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getAllTransactions(0, 20);


        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verify(inventoryTransactionRepository, times(1)).count();
        verify(inventoryTransactionRepository, times(1)).findAll();
    }

    @Test
    void getTransactionById_WithValidId_ShouldReturnTransaction() {

        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(storageUnitService.getEntityById(1L)).thenReturn(testStorageUnit);
        when(storageUnitService.getEntityById(2L)).thenReturn(testStorageUnit);

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testResponseDTO);


        InventoryTransactionResponseDTO result = inventoryTransactionService.getTransactionById(1L);


        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Scientific Equipment", result.cargoName());
        assertEquals(TransactionType.TRANSFER, result.transactionType());
        verify(inventoryTransactionRepository, times(1)).findById(1L);
    }

    @Test
    void getTransactionById_WithInvalidId_ShouldThrowException() {

        when(inventoryTransactionRepository.findById(999L)).thenReturn(Optional.empty());


        InventoryTransactionNotFoundException exception = assertThrows(
                InventoryTransactionNotFoundException.class,
                () -> inventoryTransactionService.getTransactionById(999L)
        );

        assertEquals("Transaction not found with id: 999", exception.getMessage());
        verify(inventoryTransactionRepository, times(1)).findById(999L);
    }

    @Test
    void getCargoHistory_WithValidCargoId_ShouldReturnPageResponse() {

        List<InventoryTransaction> transactions = List.of(testTransaction);
        when(inventoryTransactionRepository.findByCargoIdOrderByTransactionDate(1L)).thenReturn(transactions);
        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(storageUnitService.getEntityById(1L)).thenReturn(testStorageUnit);
        when(storageUnitService.getEntityById(2L)).thenReturn(testStorageUnit);

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testResponseDTO);


        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getCargoHistory(1L, 0, 20);


        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(20, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        verify(inventoryTransactionRepository, times(1)).findByCargoIdOrderByTransactionDate(1L);
    }

    @Test
    void getCargoHistory_WithNoTransactions_ShouldReturnEmptyPage() {

        when(inventoryTransactionRepository.findByCargoIdOrderByTransactionDate(1L)).thenReturn(List.of());


        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getCargoHistory(1L, 0, 20);


        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verify(inventoryTransactionRepository, times(1)).findByCargoIdOrderByTransactionDate(1L);
    }

//    @Test
//    void transferBetweenStorages_WithNullStorageUnits_ShouldThrowException() {
//
//        InventoryTransactionRequestDTO requestWithNullStorages = new InventoryTransactionRequestDTO(
//                TransactionType.TRANSFER, 1L, 100, null, null, null, null, 1L, "TRANSFER", "REF-001", "Invalid transfer"
//        );
//
//        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
//        when(userService.getEntityById(1L)).thenReturn(testUser);
//
//
//        IllegalArgumentException exception = assertThrows(
//                IllegalArgumentException.class,
//                () -> inventoryTransactionService.transferBetweenStorages(requestWithNullStorages)
//        );
//
//        assertEquals("Both source and destination storage units are required for transfer", exception.getMessage());
//        verify(inventoryTransactionRepository, never()).save(any());
//    }

    @Test
    void transferBetweenStorages_WithInvalidCargo_ShouldThrowException() {

        when(cargoService.getEntityById(999L)).thenThrow(new DataNotFoundException("Cargo not found"));

        InventoryTransactionRequestDTO requestWithInvalidCargo = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 999L, 100, 1L, 2L, null, null, 1L, "TRANSFER", "REF-001", "Transfer"
        );


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.transferBetweenStorages(requestWithInvalidCargo)
        );

        assertEquals("Cargo not found", exception.getMessage());
        verify(cargoService, times(1)).getEntityById(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void transferBetweenStorages_WithInvalidUser_ShouldThrowException() {

        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(userService.getEntityById(999L)).thenThrow(new DataNotFoundException("User not found"));

        InventoryTransactionRequestDTO requestWithInvalidUser = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 1L, 100, 1L, 2L, null, null, 999L, "TRANSFER", "REF-001", "Transfer"
        );


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.transferBetweenStorages(requestWithInvalidUser)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).getEntityById(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void transferBetweenStorages_WithInvalidFromStorage_ShouldThrowException() {

        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(storageUnitService.getEntityById(999L)).thenThrow(new DataNotFoundException("Source storage unit not found"));

        InventoryTransactionRequestDTO requestWithInvalidFromStorage = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 1L, 100, 999L, 2L, null, null, 1L, "TRANSFER", "REF-001", "Transfer"
        );


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.transferBetweenStorages(requestWithInvalidFromStorage)
        );

        assertEquals("Source storage unit not found", exception.getMessage());
        verify(storageUnitService, times(1)).getEntityById(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void transferBetweenStorages_WithInvalidToStorage_ShouldThrowException() {

        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(storageUnitService.getEntityById(1L)).thenReturn(testStorageUnit);
        when(storageUnitService.getEntityById(999L)).thenThrow(new DataNotFoundException("Target storage unit not found"));

        InventoryTransactionRequestDTO requestWithInvalidToStorage = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 1L, 100, 1L, 999L, null, null, 1L, "TRANSFER", "REF-001", "Transfer"
        );


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.transferBetweenStorages(requestWithInvalidToStorage)
        );

        assertEquals("Target storage unit not found", exception.getMessage());
        verify(storageUnitService, times(1)).getEntityById(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void getLocationName_WithStorageUnit_ShouldReturnStorageLocation() {

        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(storageUnitService.getEntityById(1L)).thenReturn(testStorageUnit);
        when(storageUnitService.getEntityById(2L)).thenReturn(testStorageUnit);

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testResponseDTO);


        InventoryTransactionResponseDTO result = inventoryTransactionService.getTransactionById(1L);


        assertNotNull(result);
        assertEquals("Storage: SU-001", result.fromLocation());
        assertEquals("Storage: SU-002", result.toLocation());
        verify(storageUnitService, times(1)).getEntityById(1L);
        verify(storageUnitService, times(1)).getEntityById(2L);
    }

    @Test
    void getLocationName_WithSpacecraft_ShouldReturnSpacecraftLocation() {

        InventoryTransaction spacecraftTransaction = InventoryTransaction.builder()
                .id(2L)
                .cargoId(1L)
                .performedByUserId(1L)
                .fromSpacecraftId(1L)
                .toSpacecraftId(2L)
                .quantity(100)
                .transactionType(TransactionType.TRANSFER)
                .transactionDate(LocalDateTime.now())
                .build();

        InventoryTransactionResponseDTO spacecraftResponseDTO = new InventoryTransactionResponseDTO(
                2L, TransactionType.TRANSFER, "Scientific Equipment", 100,
                "Spacecraft: Enterprise", "Spacecraft: Enterprise", "John Doe",
                LocalDateTime.now(), "TRANSFER", "REF-001", "Spacecraft transfer"
        );

        when(inventoryTransactionRepository.findById(2L)).thenReturn(Optional.of(spacecraftTransaction));
        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(spacecraftService.getEntityById(1L)).thenReturn(testSpacecraft);
        when(spacecraftService.getEntityById(2L)).thenReturn(testSpacecraft);

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(spacecraftResponseDTO);


        InventoryTransactionResponseDTO result = inventoryTransactionService.getTransactionById(2L);


        assertNotNull(result);
        assertEquals("Spacecraft: Enterprise", result.fromLocation());
        assertEquals("Spacecraft: Enterprise", result.toLocation());
        verify(spacecraftService, times(1)).getEntityById(1L);
        verify(spacecraftService, times(1)).getEntityById(2L);
    }

    @Test
    void getLocationName_WithUnknownLocation_ShouldReturnUnknown() {

        InventoryTransaction unknownTransaction = InventoryTransaction.builder()
                .id(3L)
                .cargoId(1L)
                .performedByUserId(1L)
                .fromStorageUnitId(null)
                .toStorageUnitId(null)
                .fromSpacecraftId(null)
                .toSpacecraftId(null)
                .quantity(100)
                .transactionType(TransactionType.TRANSFER)
                .transactionDate(LocalDateTime.now())
                .build();

        InventoryTransactionResponseDTO unknownResponseDTO = new InventoryTransactionResponseDTO(
                3L, TransactionType.TRANSFER, "Scientific Equipment", 100,
                "Unknown Location", "Unknown Location", "John Doe",
                LocalDateTime.now(), "TRANSFER", "REF-001", "Unknown transfer"
        );

        when(inventoryTransactionRepository.findById(3L)).thenReturn(Optional.of(unknownTransaction));
        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(userService.getEntityById(1L)).thenReturn(testUser);

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(unknownResponseDTO);


        InventoryTransactionResponseDTO result = inventoryTransactionService.getTransactionById(3L);


        assertNotNull(result);
        assertEquals("Unknown Location", result.fromLocation());
        assertEquals("Unknown Location", result.toLocation());
    }

    @Test
    void toResponseDTO_WithInvalidCargo_ShouldThrowException() {

        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(cargoService.getEntityById(1L)).thenThrow(new DataNotFoundException("Cargo not found"));


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.getTransactionById(1L)
        );

        assertEquals("Cargo not found", exception.getMessage());
        verify(cargoService, times(1)).getEntityById(1L);
    }

    @Test
    void toResponseDTO_WithInvalidUser_ShouldThrowException() {

        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
        when(userService.getEntityById(1L)).thenThrow(new DataNotFoundException("User not found"));


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.getTransactionById(1L)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).getEntityById(1L);
    }

//    @Test
//    void createInventoryTransaction_WithValidRequest_ShouldSaveTransaction() {
//
//        when(cargoService.getEntityById(1L)).thenReturn(testCargo);
//        when(userService.getEntityById(1L)).thenReturn(testUser);
//        when(storageUnitService.getEntityById(1L)).thenReturn(testStorageUnit);
//        when(storageUnitService.getEntityById(2L)).thenReturn(testStorageUnit);
//
//
//        when(inventoryTransactionMapper.toEntity(testRequestDTO)).thenReturn(testTransaction);
//        when(inventoryTransactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);
//        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
//                .thenReturn(testResponseDTO);
//
//
//        InventoryTransactionResponseDTO result = inventoryTransactionService.transferBetweenStorages(testRequestDTO);
//
//
//        assertNotNull(result);
//        assertEquals(1L, result.id());
//        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransaction.class));
//    }

    @Test
    void getAllTransactions_WithPagination_ShouldReturnCorrectPage() {

        List<InventoryTransaction> transactions = List.of(testTransaction);
        when(inventoryTransactionRepository.count()).thenReturn(5L);
        when(inventoryTransactionRepository.findAll()).thenReturn(transactions);


        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getAllTransactions(1, 2);


        assertNotNull(result);
        assertEquals(1, result.currentPage());
        assertEquals(2, result.pageSize());
        assertEquals(5, result.totalElements());
        assertEquals(3, result.totalPages());
    }
}
