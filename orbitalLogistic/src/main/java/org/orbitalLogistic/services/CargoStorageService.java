package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoStorageRequestDTO;
import org.orbitalLogistic.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.entities.CargoStorage;
import org.orbitalLogistic.entities.Cargo;
import org.orbitalLogistic.entities.StorageUnit;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.exceptions.CargoStorageNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.CargoStorageMapper;
import org.orbitalLogistic.repositories.CargoStorageRepository;
import org.orbitalLogistic.repositories.CargoRepository;
import org.orbitalLogistic.repositories.StorageUnitRepository;
import org.orbitalLogistic.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CargoStorageService {

    private final CargoStorageRepository cargoStorageRepository;
    private final CargoRepository cargoRepository;
    private final StorageUnitRepository storageUnitRepository;
    private final UserRepository userRepository;
    private final CargoStorageMapper cargoStorageMapper;

    public PageResponseDTO<CargoStorageResponseDTO> getAllCargoStorage(int page, int size) {
        long total = cargoStorageRepository.count();
        List<CargoStorage> cargoStorages = (List<CargoStorage>) cargoStorageRepository.findAll();

        List<CargoStorageResponseDTO> storageDTOs = cargoStorages.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(storageDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public PageResponseDTO<CargoStorageResponseDTO> getStorageUnitCargo(Long storageUnitId, int page, int size) {
        List<CargoStorage> cargoStorages = cargoStorageRepository.findByStorageUnitIdOrderByStoredAt(storageUnitId);

        List<CargoStorageResponseDTO> storageDTOs = cargoStorages.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) cargoStorages.size() / size);
        return new PageResponseDTO<>(storageDTOs, page, size, cargoStorages.size(), totalPages, page == 0, page >= totalPages - 1);
    }

    /**
     * Требует @Transactional, так как метод выполняет либо INSERT, либо UPDATE
     * в зависимости от наличия груза в хранилище. Также выполняется проверка
     * наличия всех связанных сущностей. Все операции должны быть атомарными.
     */
    @Transactional
    public CargoStorageResponseDTO addCargoToStorage(CargoStorageRequestDTO request) {
        validateEntities(request);

        List<CargoStorage> existing = cargoStorageRepository.findByStorageUnitIdAndCargoId(
                request.storageUnitId(), request.cargoId());

        CargoStorage cargoStorage;
        if (!existing.isEmpty()) {
            cargoStorage = existing.get(0);
            cargoStorage.setQuantity(cargoStorage.getQuantity() + request.quantity());
        } else {
            cargoStorage = cargoStorageMapper.toEntity(request);
        }

        CargoStorage saved = cargoStorageRepository.save(cargoStorage);
        return toResponseDTO(saved);
    }

    public CargoStorageResponseDTO updateQuantity(Long id, CargoStorageRequestDTO request) {
        CargoStorage cargoStorage = cargoStorageRepository.findById(id)
                .orElseThrow(() -> new CargoStorageNotFoundException("Cargo storage not found with id: " + id));

        cargoStorage.setQuantity(request.quantity());
        cargoStorage.setLastInventoryCheck(LocalDateTime.now());

        if (request.updatedByUserId() != null) {
            cargoStorage.setLastCheckedByUserId(request.updatedByUserId());
        }

        CargoStorage updated = cargoStorageRepository.save(cargoStorage);
        return toResponseDTO(updated);
    }

    private void validateEntities(CargoStorageRequestDTO request) {
        cargoRepository.findById(request.cargoId())
                .orElseThrow(() -> new DataNotFoundException("Cargo not found"));

        storageUnitRepository.findById(request.storageUnitId())
                .orElseThrow(() -> new DataNotFoundException("Storage unit not found"));

        if (request.updatedByUserId() != null) {
            userRepository.findById(request.updatedByUserId())
                    .orElseThrow(() -> new DataNotFoundException("User not found"));
        }
    }

    private CargoStorageResponseDTO toResponseDTO(CargoStorage cargoStorage) {
        StorageUnit storageUnit = storageUnitRepository.findById(cargoStorage.getStorageUnitId())
                .orElseThrow(() -> new DataNotFoundException("Storage unit not found"));

        Cargo cargo = cargoRepository.findById(cargoStorage.getCargoId())
                .orElseThrow(() -> new DataNotFoundException("Cargo not found"));

        String lastCheckedByUserName = null;
        if (cargoStorage.getLastCheckedByUserId() != null) {
            User user = userRepository.findById(cargoStorage.getLastCheckedByUserId()).orElse(null);
            if (user != null) {
                lastCheckedByUserName = user.getUsername();
            }
        }

        return cargoStorageMapper.toResponseDTO(cargoStorage,
                storageUnit.getUnitCode(),
                storageUnit.getLocation(),
                cargo.getName(),
                lastCheckedByUserName);
    }
}
