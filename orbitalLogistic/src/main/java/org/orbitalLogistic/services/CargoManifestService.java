package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
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
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.CargoManifestMapper;
import org.orbitalLogistic.repositories.CargoManifestRepository;
import org.orbitalLogistic.repositories.SpacecraftRepository;
import org.orbitalLogistic.repositories.CargoRepository;
import org.orbitalLogistic.repositories.StorageUnitRepository;
import org.orbitalLogistic.repositories.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CargoManifestService {

    private final CargoManifestRepository cargoManifestRepository;
    private final SpacecraftRepository spacecraftRepository;
    private final CargoRepository cargoRepository;
    private final StorageUnitRepository storageUnitRepository;
    private final UserRepository userRepository;
    private final CargoManifestMapper cargoManifestMapper;
    private final JdbcTemplate jdbcTemplate;


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
        spacecraftRepository.findById(spacecraftId)
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found with id: " + spacecraftId));

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
                        .cargoId(item.cargoId())
                        .storageUnitId(item.storageUnitId())
                        .quantity(item.quantity())
                        .priority(item.priority() != null ? item.priority() : org.orbitalLogistic.entities.enums.ManifestPriority.NORMAL)
                        .loadedByUserId(request.loadedByUserId())
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
        spacecraftRepository.findById(spacecraftId)
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found with id: " + spacecraftId));

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
        spacecraftRepository.findById(spacecraftId)
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found"));

        cargoRepository.findById(request.cargoId())
                .orElseThrow(() -> new DataNotFoundException("Cargo not found"));

        storageUnitRepository.findById(request.storageUnitId())
                .orElseThrow(() -> new DataNotFoundException("Storage unit not found"));

        userRepository.findById(request.loadedByUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found"));
    }

    public CargoManifestResponseDTO toResponseDTO(CargoManifest manifest) {
        Spacecraft spacecraft = spacecraftRepository.findById(manifest.getSpacecraftId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found"));

        Cargo cargo = cargoRepository.findById(manifest.getCargoId())
                .orElseThrow(() -> new DataNotFoundException("Cargo not found"));

        StorageUnit storageUnit = storageUnitRepository.findById(manifest.getStorageUnitId())
                .orElseThrow(() -> new DataNotFoundException("Storage unit not found"));

        User loadedByUser = userRepository.findById(manifest.getLoadedByUserId())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        String unloadedByUserName = null;
        if (manifest.getUnloadedByUserId() != null) {
            User unloadedByUser = userRepository.findById(manifest.getUnloadedByUserId()).orElse(null);
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
