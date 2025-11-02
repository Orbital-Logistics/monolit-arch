package org.orbitalLogistic.services;

import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoManifestRequestDTO;
import org.orbitalLogistic.dto.response.CargoManifestResponseDTO;
import org.orbitalLogistic.entities.CargoManifest;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.Cargo;
import org.orbitalLogistic.entities.StorageUnit;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.enums.ManifestStatus;
import org.orbitalLogistic.exceptions.CargoManifestNotFoundException;
import org.orbitalLogistic.mappers.CargoManifestMapper;
import org.orbitalLogistic.repositories.CargoManifestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class CargoManifestService {

    private final CargoManifestRepository cargoManifestRepository;
    private final CargoManifestMapper cargoManifestMapper;
    private final JdbcTemplate jdbcTemplate;

    private SpacecraftService spacecraftService;
    private CargoService cargoService;
    private StorageUnitService storageUnitService;
    private UserService userService;

    public CargoManifestService(CargoManifestRepository cargoManifestRepository,
                                CargoManifestMapper cargoManifestMapper,
                                JdbcTemplate jdbcTemplate) {
        this.cargoManifestRepository = cargoManifestRepository;
        this.cargoManifestMapper = cargoManifestMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    public void setSpacecraftService(@Lazy SpacecraftService spacecraftService) {
        this.spacecraftService = spacecraftService;
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


    public PageResponseDTO<CargoManifestResponseDTO> getAllManifests(int page, int size) {
        long total = cargoManifestRepository.count();
        List<CargoManifest> manifests = (List<CargoManifest>) cargoManifestRepository.findAll();

        List<CargoManifestResponseDTO> manifestDTOs = manifests.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(manifestDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public CargoManifestResponseDTO getManifestById(Long id) {
        CargoManifest manifest = cargoManifestRepository.findById(id)
                .orElseThrow(() -> new CargoManifestNotFoundException("Cargo manifest not found with id: " + id));
        return toResponseDTO(manifest);
    }

    public PageResponseDTO<CargoManifestResponseDTO> getSpacecraftManifest(Long spacecraftId, int page, int size) {
        List<CargoManifest> manifests = cargoManifestRepository.findBySpacecraftIdOrderByPriorityAndLoadedAt(spacecraftId);

        List<CargoManifestResponseDTO> manifestDTOs = manifests.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) manifests.size() / size);
        return new PageResponseDTO<>(manifestDTOs, page, size, manifests.size(), totalPages, page == 0, page >= totalPages - 1);
    }

    /**
     * Требует @Transactional, так как выполняет множественные операции записи
     * (создание нескольких манифестов) в рамках одной бизнес-транзакции.
     * При ошибке на любом шаге все изменения должны быть откатаны.
     */
    @Transactional
    public List<CargoManifestResponseDTO> loadCargoToSpacecraft(Long spacecraftId, CargoManifestRequestDTO request) {
        spacecraftService.getSpacecraftById(request.spacecraftId());
        
        List<CargoManifestResponseDTO> results = new ArrayList<>();

        if (request.cargoId() != null) {
            CargoManifest manifest = createManifest(request, spacecraftId);
            manifest.setManifestStatus(ManifestStatus.LOADED);
            manifest.setLoadedAt(LocalDateTime.now());
            CargoManifest saved = cargoManifestRepository.save(manifest);
            results.add(toResponseDTO(saved));
        }

        if (request.cargoItems() != null && !request.cargoItems().isEmpty()) {
            for (CargoManifestRequestDTO.CargoItemDTO item : request.cargoItems()) {
                CargoManifest manifest = CargoManifest.builder()
                        .spacecraftId(spacecraftId)
                        .cargoId(cargoService.getCargoById(item.cargoId()).id())
                        .storageUnitId(storageUnitService.getStorageUnitById(item.storageUnitId()).id())
                        .quantity(item.quantity())
                        .priority(item.priority() != null ? item.priority() : org.orbitalLogistic.entities.enums.ManifestPriority.NORMAL)
                        .loadedByUserId(userService.findUserById(request.loadedByUserId()).id())
                        .manifestStatus(ManifestStatus.LOADED)
                        .loadedAt(LocalDateTime.now())
                        .build();

                CargoManifest saved = cargoManifestRepository.save(manifest);
                results.add(toResponseDTO(saved));
            }
        }

        return results;
    }

    /**
     * Требует @Transactional, так как выполняет множественные UPDATE операции
     * для выгрузки всего груза с космического корабля. Все операции должны
     * выполниться атомарно - либо весь груз выгружен, либо ничего.
     */
    @Transactional
    public List<CargoManifestResponseDTO> unloadCargoFromSpacecraft(Long spacecraftId, CargoManifestRequestDTO request) {
        spacecraftService.getEntityById(spacecraftId);
        userService.getEntityById(request.loadedByUserId());
        userService.getEntityById(request.unloadedByUserId());
        storageUnitService.getEntityById(request.storageUnitId());
        storageUnitService.getEntityById(request.unloadedByUserId());
        cargoService.getCargoById(request.cargoId());
        List<CargoManifest> activeManifests = cargoManifestRepository.findActiveCargoBySpacecraft(spacecraftId);

        List<CargoManifestResponseDTO> results = new ArrayList<>();
        for (CargoManifest manifest : activeManifests) {
            String sql = "UPDATE cargo_manifest SET " +
                         "manifest_status = ?::manifest_status_enum, " +
                         "unloaded_at = ?, " +
                         "unloaded_by_user_id = ? " +
                         "WHERE id = ?";
            
            jdbcTemplate.update(sql,
                    ManifestStatus.UNLOADED.name(),
                    LocalDateTime.now(),
                    request.unloadedByUserId(),
                    manifest.getId()
            );

            manifest.setManifestStatus(ManifestStatus.UNLOADED);
            manifest.setUnloadedAt(LocalDateTime.now());
            manifest.setUnloadedByUserId(request.unloadedByUserId());

            results.add(toResponseDTO(manifest));
        }

        return results;
    }

    private CargoManifest createManifest(CargoManifestRequestDTO request, Long spacecraftId) {
        validateEntities(request, spacecraftId);
        return cargoManifestMapper.toEntity(request);
    }

    private void validateEntities(CargoManifestRequestDTO request, Long spacecraftId) {
        spacecraftService.getEntityById(spacecraftId);
        cargoService.getEntityById(request.cargoId());
        storageUnitService.getEntityById(request.storageUnitId());
        userService.getEntityById(request.loadedByUserId());
    }

    public boolean existsByCargoId(Long cargoId) {
        return cargoManifestRepository.existsByCargoId(cargoId);
    }

    public CargoManifestResponseDTO toResponseDTO(CargoManifest manifest) {
        Spacecraft spacecraft = spacecraftService.getEntityById(manifest.getSpacecraftId());
        Cargo cargo = cargoService.getEntityById(manifest.getCargoId());
        StorageUnit storageUnit = storageUnitService.getEntityById(manifest.getStorageUnitId());
        User loadedByUser = userService.getEntityById(manifest.getLoadedByUserId());

        String unloadedByUserName = null;
        if (manifest.getUnloadedByUserId() != null) {
            User unloadedByUser = userService.getEntityByIdOrNull(manifest.getUnloadedByUserId());
            if (unloadedByUser != null) {
                unloadedByUserName = unloadedByUser.getUsername();
            }
        }

        return cargoManifestMapper.toResponseDTO(manifest,
                spacecraft.getName(),
                cargo.getName(),
                storageUnit.getUnitCode(),
                loadedByUser.getUsername(),
                unloadedByUserName);
    }
}
