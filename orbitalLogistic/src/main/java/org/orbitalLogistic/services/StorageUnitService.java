package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.dto.response.StorageUnitResponseDTO;
import org.orbitalLogistic.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.entities.StorageUnit;
import org.orbitalLogistic.entities.CargoStorage;
import org.orbitalLogistic.exceptions.StorageUnitAlreadyExistsException;
import org.orbitalLogistic.exceptions.StorageUnitNotFoundException;
import org.orbitalLogistic.mappers.StorageUnitMapper;
import org.orbitalLogistic.mappers.CargoStorageMapper;
import org.orbitalLogistic.repositories.StorageUnitRepository;
import org.orbitalLogistic.repositories.CargoStorageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StorageUnitService {

    private final StorageUnitRepository storageUnitRepository;
    private final CargoStorageRepository cargoStorageRepository;
    private final StorageUnitMapper storageUnitMapper;
    private final CargoStorageMapper cargoStorageMapper;

    @Transactional(readOnly = true)
    public PageResponseDTO<StorageUnitResponseDTO> getStorageUnits(int page, int size) {
        int offset = page * size;
        List<StorageUnit> storageUnits = storageUnitRepository.findAllPaged(size, offset);
        long total = storageUnitRepository.countAll();

        List<StorageUnitResponseDTO> storageUnitDTOs = storageUnits.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(storageUnitDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    @Transactional(readOnly = true)
    public StorageUnitResponseDTO getStorageUnitById(Long id) {
        StorageUnit storageUnit = storageUnitRepository.findById(id)
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + id));
        return toResponseDTO(storageUnit);
    }

    public StorageUnitResponseDTO createStorageUnit(StorageUnitRequestDTO request) {
        if (storageUnitRepository.existsByUnitCode(request.unitCode())) {
            throw new StorageUnitAlreadyExistsException("Storage unit with code already exists: " + request.unitCode());
        }

        StorageUnit storageUnit = storageUnitMapper.toEntity(request);
        StorageUnit saved = storageUnitRepository.save(storageUnit);
        return toResponseDTO(saved);
    }

    public StorageUnitResponseDTO updateStorageUnit(Long id, StorageUnitRequestDTO request) {
        StorageUnit storageUnit = storageUnitRepository.findById(id)
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + id));

        if (!storageUnit.getUnitCode().equals(request.unitCode()) && 
            storageUnitRepository.existsByUnitCode(request.unitCode())) {
            throw new StorageUnitAlreadyExistsException("Storage unit with code already exists: " + request.unitCode());
        }

        storageUnit.setUnitCode(request.unitCode());
        storageUnit.setLocation(request.location());
        storageUnit.setStorageType(request.storageType());
        storageUnit.setTotalMassCapacity(request.totalMassCapacity());
        storageUnit.setTotalVolumeCapacity(request.totalVolumeCapacity());

        StorageUnit updated = storageUnitRepository.save(storageUnit);
        return toResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<CargoStorageResponseDTO> getStorageUnitInventory(Long id, int page, int size) {
        if (!storageUnitRepository.existsById(id)) {
            throw new StorageUnitNotFoundException("Storage unit not found with id: " + id);
        }

        int offset = page * size;
        List<CargoStorage> cargoStorageList = cargoStorageRepository.findByStorageUnitIdOrderByStoredAt(id);

        List<CargoStorage> pagedList = cargoStorageList.stream()
                .skip(offset)
                .limit(size)
                .toList();

        long total = cargoStorageRepository.countByStorageUnitId(id);

        List<CargoStorageResponseDTO> responseDTOs = pagedList.stream()
                .map(cargoStorageMapper::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(
            responseDTOs,
            page,
            size,
            total,
            totalPages,
            page == 0,
            page >= totalPages - 1
        );
    }

    private StorageUnitResponseDTO toResponseDTO(StorageUnit storageUnit) {
        BigDecimal availableMassCapacity = storageUnit.getTotalMassCapacity().subtract(storageUnit.getCurrentMass());
        BigDecimal availableVolumeCapacity = storageUnit.getTotalVolumeCapacity().subtract(storageUnit.getCurrentVolume());

        Double massUsagePercentage = storageUnit.getTotalMassCapacity().compareTo(BigDecimal.ZERO) > 0 ?
                storageUnit.getCurrentMass().divide(storageUnit.getTotalMassCapacity(), 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100 : 0.0;

        Double volumeUsagePercentage = storageUnit.getTotalVolumeCapacity().compareTo(BigDecimal.ZERO) > 0 ?
                storageUnit.getCurrentVolume().divide(storageUnit.getTotalVolumeCapacity(), 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100 : 0.0;

        return storageUnitMapper.toResponseDTO(
            storageUnit,
            availableMassCapacity,
            availableVolumeCapacity,
            massUsagePercentage,
            volumeUsagePercentage
        );
    }
}
