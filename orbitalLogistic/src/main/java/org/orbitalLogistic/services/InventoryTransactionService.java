package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.InventoryTransactionRequestDTO;
import org.orbitalLogistic.dto.response.InventoryTransactionResponseDTO;
import org.orbitalLogistic.entities.InventoryTransaction;
import org.orbitalLogistic.entities.Cargo;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.enums.TransactionType;
import org.orbitalLogistic.exceptions.InventoryTransactionNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.InventoryTransactionMapper;
import org.orbitalLogistic.repositories.InventoryTransactionRepository;
import org.orbitalLogistic.repositories.CargoRepository;
import org.orbitalLogistic.repositories.StorageUnitRepository;
import org.orbitalLogistic.repositories.SpacecraftRepository;
import org.orbitalLogistic.repositories.UserRepository;
import org.orbitalLogistic.repositories.CargoManifestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryTransactionService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final CargoRepository cargoRepository;
    private final StorageUnitRepository storageUnitRepository;
    private final SpacecraftRepository spacecraftRepository;
    private final UserRepository userRepository;
    private final CargoManifestRepository cargoManifestRepository;
    private final InventoryTransactionMapper inventoryTransactionMapper;

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public InventoryTransactionResponseDTO getTransactionById(Long id) {
        InventoryTransaction transaction = inventoryTransactionRepository.findById(id)
                .orElseThrow(() -> new InventoryTransactionNotFoundException("Transaction not found with id: " + id));
        return toResponseDTO(transaction);
    }

    @Transactional(readOnly = true)
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
        cargoRepository.findById(request.cargoId())
                .orElseThrow(() -> new DataNotFoundException("Cargo not found"));

        userRepository.findById(request.performedByUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        if (request.fromStorageUnitId() != null) {
            storageUnitRepository.findById(request.fromStorageUnitId())
                    .orElseThrow(() -> new DataNotFoundException("Source storage unit not found"));
        }

        if (request.toStorageUnitId() != null) {
            storageUnitRepository.findById(request.toStorageUnitId())
                    .orElseThrow(() -> new DataNotFoundException("Target storage unit not found"));
        }

        if (request.fromSpacecraftId() != null) {
            spacecraftRepository.findById(request.fromSpacecraftId())
                    .orElseThrow(() -> new DataNotFoundException("Source spacecraft not found"));
        }

        if (request.toSpacecraftId() != null) {
            spacecraftRepository.findById(request.toSpacecraftId())
                    .orElseThrow(() -> new DataNotFoundException("Target spacecraft not found"));
        }
    }

    private InventoryTransactionResponseDTO toResponseDTO(InventoryTransaction transaction) {
        Cargo cargo = cargoRepository.findById(transaction.getCargoId())
                .orElseThrow(() -> new DataNotFoundException("Cargo not found"));

        User performedByUser = userRepository.findById(transaction.getPerformedByUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        String fromLocation = getLocationName(transaction.getFromStorageUnitId(), transaction.getFromSpacecraftId());
        String toLocation = getLocationName(transaction.getToStorageUnitId(), transaction.getToSpacecraftId());

        return inventoryTransactionMapper.toResponseDTO(transaction,
                cargo.getName(),
                fromLocation,
                toLocation,
                performedByUser.getFirst_name() + " " + performedByUser.getLast_name());
    }

    private String getLocationName(Long storageUnitId, Long spacecraftId) {
        if (storageUnitId != null) {
            return storageUnitRepository.findById(storageUnitId)
                    .map(unit -> "Storage: " + unit.getUnitCode())
                    .orElse("Unknown Storage");
        }
        if (spacecraftId != null) {
            return spacecraftRepository.findById(spacecraftId)
                    .map(spacecraft -> "Spacecraft: " + spacecraft.getName())
                    .orElse("Unknown Spacecraft");
        }
        return "Unknown Location";
    }
}
