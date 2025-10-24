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
    private CargoRepository cargoRepository;

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @Mock
    private SpacecraftRepository spacecraftRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CargoManifestRepository cargoManifestRepository;

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
    }

    @Test
    void getAllTransactions_WithValidParameters_ShouldReturnPageResponse() {
        // Given
        List<InventoryTransaction> transactions = List.of(testTransaction);
        when(inventoryTransactionRepository.count()).thenReturn(1L);
        when(inventoryTransactionRepository.findAll()).thenReturn(transactions);
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitRepository.findById(2L)).thenReturn(Optional.of(testStorageUnit));

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testResponseDTO);

        // When
        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getAllTransactions(0, 20);

        // Then
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
        // Given
        when(inventoryTransactionRepository.count()).thenReturn(0L);
        when(inventoryTransactionRepository.findAll()).thenReturn(List.of());

        // When
        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getAllTransactions(0, 20);

        // Then
        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verify(inventoryTransactionRepository, times(1)).count();
        verify(inventoryTransactionRepository, times(1)).findAll();
    }

    @Test
    void getTransactionById_WithValidId_ShouldReturnTransaction() {
        // Given
        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitRepository.findById(2L)).thenReturn(Optional.of(testStorageUnit));

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testResponseDTO);

        // When
        InventoryTransactionResponseDTO result = inventoryTransactionService.getTransactionById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Scientific Equipment", result.cargoName());
        assertEquals(TransactionType.TRANSFER, result.transactionType());
        verify(inventoryTransactionRepository, times(1)).findById(1L);
    }

    @Test
    void getTransactionById_WithInvalidId_ShouldThrowException() {
        // Given
        when(inventoryTransactionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        InventoryTransactionNotFoundException exception = assertThrows(
                InventoryTransactionNotFoundException.class,
                () -> inventoryTransactionService.getTransactionById(999L)
        );

        assertEquals("Transaction not found with id: 999", exception.getMessage());
        verify(inventoryTransactionRepository, times(1)).findById(999L);
    }

    @Test
    void getCargoHistory_WithValidCargoId_ShouldReturnPageResponse() {
        // Given
        List<InventoryTransaction> transactions = List.of(testTransaction);
        when(inventoryTransactionRepository.findByCargoIdOrderByTransactionDate(1L)).thenReturn(transactions);
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitRepository.findById(2L)).thenReturn(Optional.of(testStorageUnit));

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testResponseDTO);

        // When
        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getCargoHistory(1L, 0, 20);

        // Then
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
        // Given
        when(inventoryTransactionRepository.findByCargoIdOrderByTransactionDate(1L)).thenReturn(List.of());

        // When
        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getCargoHistory(1L, 0, 20);

        // Then
        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verify(inventoryTransactionRepository, times(1)).findByCargoIdOrderByTransactionDate(1L);
    }

    @Test
    void transferBetweenStorages_WithNullStorageUnits_ShouldThrowException() {
        // Given
        InventoryTransactionRequestDTO requestWithNullStorages = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 1L, 100, null, null, null, null, 1L, "TRANSFER", "REF-001", "Invalid transfer"
        );

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryTransactionService.transferBetweenStorages(requestWithNullStorages)
        );

        assertEquals("Both source and destination storage units are required for transfer", exception.getMessage());
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void transferBetweenStorages_WithInvalidCargo_ShouldThrowException() {
        // Given
        when(cargoRepository.findById(999L)).thenReturn(Optional.empty());

        InventoryTransactionRequestDTO requestWithInvalidCargo = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 999L, 100, 1L, 2L, null, null, 1L, "TRANSFER", "REF-001", "Transfer"
        );

        // When & Then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.transferBetweenStorages(requestWithInvalidCargo)
        );

        assertEquals("Cargo not found", exception.getMessage());
        verify(cargoRepository, times(1)).findById(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void transferBetweenStorages_WithInvalidUser_ShouldThrowException() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        InventoryTransactionRequestDTO requestWithInvalidUser = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 1L, 100, 1L, 2L, null, null, 999L, "TRANSFER", "REF-001", "Transfer"
        );

        // When & Then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.transferBetweenStorages(requestWithInvalidUser)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void transferBetweenStorages_WithInvalidFromStorage_ShouldThrowException() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(storageUnitRepository.findById(999L)).thenReturn(Optional.empty());

        InventoryTransactionRequestDTO requestWithInvalidFromStorage = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 1L, 100, 999L, 2L, null, null, 1L, "TRANSFER", "REF-001", "Transfer"
        );

        // When & Then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.transferBetweenStorages(requestWithInvalidFromStorage)
        );

        assertEquals("Source storage unit not found", exception.getMessage());
        verify(storageUnitRepository, times(1)).findById(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void transferBetweenStorages_WithInvalidToStorage_ShouldThrowException() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitRepository.findById(999L)).thenReturn(Optional.empty());

        InventoryTransactionRequestDTO requestWithInvalidToStorage = new InventoryTransactionRequestDTO(
                TransactionType.TRANSFER, 1L, 100, 1L, 999L, null, null, 1L, "TRANSFER", "REF-001", "Transfer"
        );

        // When & Then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.transferBetweenStorages(requestWithInvalidToStorage)
        );

        assertEquals("Target storage unit not found", exception.getMessage());
        verify(storageUnitRepository, times(1)).findById(999L);
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void getLocationName_WithStorageUnit_ShouldReturnStorageLocation() {
        // Given
        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitRepository.findById(2L)).thenReturn(Optional.of(testStorageUnit));

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testResponseDTO);

        // When
        InventoryTransactionResponseDTO result = inventoryTransactionService.getTransactionById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Storage: SU-001", result.fromLocation());
        assertEquals("Storage: SU-002", result.toLocation());
        verify(storageUnitRepository, times(1)).findById(1L);
        verify(storageUnitRepository, times(1)).findById(2L);
    }

    @Test
    void getLocationName_WithSpacecraft_ShouldReturnSpacecraftLocation() {
        // Given
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
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftRepository.findById(2L)).thenReturn(Optional.of(testSpacecraft));

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(spacecraftResponseDTO);

        // When
        InventoryTransactionResponseDTO result = inventoryTransactionService.getTransactionById(2L);

        // Then
        assertNotNull(result);
        assertEquals("Spacecraft: Enterprise", result.fromLocation());
        assertEquals("Spacecraft: Enterprise", result.toLocation());
        verify(spacecraftRepository, times(1)).findById(1L);
        verify(spacecraftRepository, times(1)).findById(2L);
    }

    @Test
    void getLocationName_WithUnknownLocation_ShouldReturnUnknown() {
        // Given
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
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(unknownResponseDTO);

        // When
        InventoryTransactionResponseDTO result = inventoryTransactionService.getTransactionById(3L);

        // Then
        assertNotNull(result);
        assertEquals("Unknown Location", result.fromLocation());
        assertEquals("Unknown Location", result.toLocation());
    }

    @Test
    void toResponseDTO_WithInvalidCargo_ShouldThrowException() {
        // Given
        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(cargoRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.getTransactionById(1L)
        );

        assertEquals("Cargo not found", exception.getMessage());
        verify(cargoRepository, times(1)).findById(1L);
    }

    @Test
    void toResponseDTO_WithInvalidUser_ShouldThrowException() {
        // Given
        when(inventoryTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> inventoryTransactionService.getTransactionById(1L)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void createInventoryTransaction_WithValidRequest_ShouldSaveTransaction() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testStorageUnit));
        when(storageUnitRepository.findById(2L)).thenReturn(Optional.of(testStorageUnit));

        // Добавляем мок для toEntity
        when(inventoryTransactionMapper.toEntity(testRequestDTO)).thenReturn(testTransaction);
        when(inventoryTransactionRepository.save(any(InventoryTransaction.class))).thenReturn(testTransaction);
        when(inventoryTransactionMapper.toResponseDTO(any(InventoryTransaction.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testResponseDTO);

        // When
        InventoryTransactionResponseDTO result = inventoryTransactionService.transferBetweenStorages(testRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransaction.class));
    }

    @Test
    void getAllTransactions_WithPagination_ShouldReturnCorrectPage() {
        // Given
        List<InventoryTransaction> transactions = List.of(testTransaction);
        when(inventoryTransactionRepository.count()).thenReturn(5L);
        when(inventoryTransactionRepository.findAll()).thenReturn(transactions);

        // When
        PageResponseDTO<InventoryTransactionResponseDTO> result = inventoryTransactionService.getAllTransactions(1, 2);

        // Then
        assertNotNull(result);
        assertEquals(1, result.currentPage());
        assertEquals(2, result.pageSize());
        assertEquals(5, result.totalElements());
        assertEquals(3, result.totalPages()); // ceil(5/2) = 3
    }
}
