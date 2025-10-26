package org.orbitalLogistic.services;

import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.dto.response.InventoryTransactionResponseDTO;
import org.orbitalLogistic.entities.InventoryTransaction;
import org.orbitalLogistic.entities.Cargo;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.StorageUnit;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.enums.TransactionType;
import org.orbitalLogistic.exceptions.InventoryTransactionNotFoundException;
import org.orbitalLogistic.mappers.InventoryTransactionMapper;
import org.orbitalLogistic.repositories.InventoryTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryTransactionService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryTransactionMapper inventoryTransactionMapper;

    private CargoService cargoService;
    private StorageUnitService storageUnitService;
    private SpacecraftService spacecraftService;
    private UserService userService;

    public InventoryTransactionService(InventoryTransactionRepository inventoryTransactionRepository,
                                      InventoryTransactionMapper inventoryTransactionMapper) {
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
    }

    @Autowired
    public void setCargoService(@Lazy CargoService cargoService) {
        this.cargoService = cargoService;
    }

    @Autowired
    public void setStorageUnitService(@Lazy StorageUnitService storageUnitService) {
        this.storageUnitService = storageUnitService;
    }

    @Autowired
    public void setSpacecraftService(@Lazy SpacecraftService spacecraftService) {
        this.spacecraftService = spacecraftService;
    }

    @Autowired
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    public PageResponseDTO<InventoryTransactionResponseDTO> getAllTransactions(int page, int size) {
        long total = inventoryTransactionRepository.count();
        List<InventoryTransaction> transactions = (List<InventoryTransaction>) inventoryTransactionRepository.findAll();

        List<InventoryTransactionResponseDTO> transactionDTOs = transactions.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(transactionDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public InventoryTransactionResponseDTO getTransactionById(Long id) {
        InventoryTransaction transaction = inventoryTransactionRepository.findById(id)
                .orElseThrow(() -> new InventoryTransactionNotFoundException("Transaction not found with id: " + id));
        return toResponseDTO(transaction);
    }

    public PageResponseDTO<InventoryTransactionResponseDTO> getCargoHistory(Long cargoId, int page, int size) {
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findByCargoIdOrderByTransactionDate(cargoId);

        List<InventoryTransactionResponseDTO> transactionDTOs = transactions.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) transactions.size() / size);
        return new PageResponseDTO<>(transactionDTOs, page, size, transactions.size(), totalPages, page == 0, page >= totalPages - 1);
    }

    /**
     * Требует @Transactional, так как перемещение груза между хранилищами должно
     * быть атомарной операцией.
     */
    @Transactional
    public InventoryTransactionResponseDTO transferBetweenStorages(InventoryTransactionRequestDTO request) {
        validateTransactionEntities(request);

        if (request.fromStorageUnitId() == null || request.toStorageUnitId() == null) {
            throw new IllegalArgumentException("Both source and destination storage units are required for transfer");
        }

        InventoryTransaction transaction = inventoryTransactionMapper.toEntity(request);
        transaction.setTransactionType(TransactionType.TRANSFER);

        InventoryTransaction saved = inventoryTransactionRepository.save(transaction);
        return toResponseDTO(saved);
    }

    private void validateTransactionEntities(InventoryTransactionRequestDTO request) {
        cargoService.getEntityById(request.cargoId());
        userService.getEntityById(request.performedByUserId());

        if (request.fromStorageUnitId() != null) {
            storageUnitService.getEntityById(request.fromStorageUnitId());
        }

        if (request.toStorageUnitId() != null) {
            storageUnitService.getEntityById(request.toStorageUnitId());
        }

        if (request.fromSpacecraftId() != null) {
            spacecraftService.getEntityById(request.fromSpacecraftId());
        }

        if (request.toSpacecraftId() != null) {
            spacecraftService.getEntityById(request.toSpacecraftId());
        }
    }

    private InventoryTransactionResponseDTO toResponseDTO(InventoryTransaction transaction) {
        Cargo cargo = cargoService.getEntityById(transaction.getCargoId());
        User performedByUser = userService.getEntityById(transaction.getPerformedByUserId());

        String fromLocation = getLocationName(transaction.getFromStorageUnitId(), transaction.getFromSpacecraftId());
        String toLocation = getLocationName(transaction.getToStorageUnitId(), transaction.getToSpacecraftId());

        return inventoryTransactionMapper.toResponseDTO(transaction,
                cargo.getName(),
                fromLocation,
                toLocation,
                performedByUser.getUsername());
    }

    private String getLocationName(Long storageUnitId, Long spacecraftId) {
        if (storageUnitId != null) {
            StorageUnit unit = storageUnitService.getEntityById(storageUnitId);
            return "Storage: " + unit.getUnitCode();
        }
        if (spacecraftId != null) {
            Spacecraft spacecraft = spacecraftService.getEntityById(spacecraftId);
            return "Spacecraft: " + spacecraft.getName();
        }
        return "Unknown Location";
    }
}
