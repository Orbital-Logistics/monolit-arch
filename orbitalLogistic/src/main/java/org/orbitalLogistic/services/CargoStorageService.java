package org.orbitalLogistic.services;

import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoStorageRequestDTO;
import org.orbitalLogistic.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.entities.CargoStorage;
import org.orbitalLogistic.entities.Cargo;
import org.orbitalLogistic.entities.StorageUnit;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.exceptions.CargoStorageNotFoundException;
import org.orbitalLogistic.exceptions.user.UserNotFoundException;
import org.orbitalLogistic.mappers.CargoStorageMapper;
import org.orbitalLogistic.repositories.CargoStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CargoStorageService {

    private final CargoStorageRepository cargoStorageRepository;
    private final CargoStorageMapper cargoStorageMapper;

    private CargoService cargoService;
    private StorageUnitService storageUnitService;
    private UserService userService;

    public CargoStorageService(CargoStorageRepository cargoStorageRepository,
                               CargoStorageMapper cargoStorageMapper) {
        this.cargoStorageRepository = cargoStorageRepository;
        this.cargoStorageMapper = cargoStorageMapper;
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
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

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
                request.storageUnitId(), cargoService.getCargoById(request.cargoId()).id());

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
        try {
            Long user_id = userService.findUserById(request.updatedByUserId()).id();
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException("updatedByUser not found with id: " + request.updatedByUserId());
        }

        CargoStorage cargoStorage = cargoStorageRepository.findById(id)
                .orElseThrow(() -> new CargoStorageNotFoundException("Cargo storage not found with id: " + id));

        cargoStorage.setQuantity(request.quantity());
        cargoStorage.setLastInventoryCheck(LocalDateTime.now());

        if (request.updatedByUserId() != null) {
            cargoStorage.setLastCheckedByUserId(userService.findUserById(request.updatedByUserId()).id());
        }

        CargoStorage updated = cargoStorageRepository.save(cargoStorage);
        return toResponseDTO(updated);
    }

    public Integer calculateTotalQuantityForCargo(Long cargoId) {
        return cargoStorageRepository.findByCargoId(cargoId)
                .stream()
                .mapToInt(storage -> storage.getQuantity() != null ? storage.getQuantity() : 0)
                .sum();
    }

    public List<CargoStorage> findByStorageUnitIdOrderByStoredAt(Long storageUnitId) {
        return cargoStorageRepository.findByStorageUnitIdOrderByStoredAt(storageUnitId);
    }

    public long countByStorageUnitId(Long storageUnitId) {
        return cargoStorageRepository.countByStorageUnitId(storageUnitId);
    }

    private void validateEntities(CargoStorageRequestDTO request) {
        cargoService.getEntityById(request.cargoId());
        storageUnitService.getEntityById(request.storageUnitId());

        if (request.updatedByUserId() != null) {
            userService.getEntityById(request.updatedByUserId());
        }
    }

    private CargoStorageResponseDTO toResponseDTO(CargoStorage cargoStorage) {
        StorageUnit storageUnit = storageUnitService.getEntityById(cargoStorage.getStorageUnitId());
        Cargo cargo = cargoService.getEntityById(cargoStorage.getCargoId());

        String lastCheckedByUserName = null;
        if (cargoStorage.getLastCheckedByUserId() != null) {
            User user = userService.getEntityByIdOrNull(cargoStorage.getLastCheckedByUserId());
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
