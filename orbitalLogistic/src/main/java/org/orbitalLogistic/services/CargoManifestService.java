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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class CargoManifestService {

    private final CargoManifestRepository cargoManifestRepository;
    private final SpacecraftRepository spacecraftRepository;
    private final CargoRepository cargoRepository;
    private final StorageUnitRepository storageUnitRepository;
    private final UserRepository userRepository;
    private final CargoManifestMapper cargoManifestMapper;

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public CargoManifestResponseDTO getManifestById(Long id) {
        CargoManifest manifest = cargoManifestRepository.findById(id)
                .orElseThrow(() -> new CargoManifestNotFoundException("Cargo manifest not found with id: " + id));
        return toResponseDTO(manifest);
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public List<CargoManifestResponseDTO> loadCargoToSpacecraft(Long spacecraftId, CargoManifestRequestDTO request) {
        // Validate spacecraft exists
        Spacecraft spacecraft = spacecraftRepository.findById(spacecraftId)
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found with id: " + spacecraftId));

        List<CargoManifestResponseDTO> results = new ArrayList<>();

        // Handle single cargo item
        if (request.cargoId() != null) {
            CargoManifest manifest = createManifest(request, spacecraftId);
            manifest.setManifestStatus(ManifestStatus.LOADED);
            manifest.setLoadedAt(LocalDateTime.now());
            CargoManifest saved = cargoManifestRepository.save(manifest);
            results.add(toResponseDTO(saved));
        }

        // Handle multiple cargo items
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

    @Transactional
    public List<CargoManifestResponseDTO> unloadCargoFromSpacecraft(Long spacecraftId, CargoManifestRequestDTO request) {
        // Validate spacecraft exists
        Spacecraft spacecraft = spacecraftRepository.findById(spacecraftId)
                .orElseThrow(() -> new DataNotFoundException("Spacecraft not found with id: " + spacecraftId));

        List<CargoManifest> activeManifests = cargoManifestRepository.findActiveCargoBySpacecraft(spacecraftId);

        // Update manifests to unloaded status
        List<CargoManifestResponseDTO> results = new ArrayList<>();
        for (CargoManifest manifest : activeManifests) {
            manifest.setManifestStatus(ManifestStatus.UNLOADED);
            manifest.setUnloadedAt(LocalDateTime.now());
            manifest.setUnloadedByUserId(request.unloadedByUserId());

            CargoManifest updated = cargoManifestRepository.save(manifest);
            results.add(toResponseDTO(updated));
        }

        return results;
    }

    private CargoManifest createManifest(CargoManifestRequestDTO request, Long spacecraftId) {
        // Validate all entities exist
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

    private CargoManifestResponseDTO toResponseDTO(CargoManifest manifest) {
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
                unloadedByUserName = unloadedByUser.getFirst_name() + " " + unloadedByUser.getLast_name();
            }
        }

        return cargoManifestMapper.toResponseDTO(manifest,
                spacecraft.getName(),
                cargo.getName(),
                storageUnit.getUnitCode(),
                loadedByUser.getFirst_name() + " " + loadedByUser.getLast_name(),
                unloadedByUserName);
    }
}
