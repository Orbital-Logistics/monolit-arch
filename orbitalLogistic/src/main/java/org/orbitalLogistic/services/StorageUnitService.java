package org.orbitalLogistic.services;

import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.dto.response.StorageUnitResponseDTO;
import org.orbitalLogistic.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.entities.StorageUnit;
import org.orbitalLogistic.exceptions.StorageUnitAlreadyExistsException;
import org.orbitalLogistic.exceptions.StorageUnitNotFoundException;
import org.orbitalLogistic.mappers.StorageUnitMapper;
import org.orbitalLogistic.repositories.StorageUnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StorageUnitService {

    private final StorageUnitRepository storageUnitRepository;
    private final StorageUnitMapper storageUnitMapper;
    private final JdbcTemplate jdbcTemplate;

    private CargoStorageService cargoStorageService;

    public StorageUnitService(StorageUnitRepository storageUnitRepository,
                             StorageUnitMapper storageUnitMapper,
                             JdbcTemplate jdbcTemplate) {
        this.storageUnitRepository = storageUnitRepository;
        this.storageUnitMapper = storageUnitMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    public void setCargoStorageService(@Lazy CargoStorageService cargoStorageService) {
        this.cargoStorageService = cargoStorageService;
    }

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

    public StorageUnitResponseDTO getStorageUnitById(Long id) {
        StorageUnit storageUnit = storageUnitRepository.findById(id)
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + id));
        return toResponseDTO(storageUnit);
    }

    public StorageUnitResponseDTO createStorageUnit(StorageUnitRequestDTO request) {
        if (storageUnitRepository.existsByUnitCode(request.unitCode())) {
            throw new StorageUnitAlreadyExistsException("Storage unit with code already exists: " + request.unitCode());
        }

        String sql = "INSERT INTO storage_unit " +
                     "(unit_code, location, storage_type, total_mass_capacity, total_volume_capacity, current_mass, current_volume) " +
                     "VALUES (?, ?, ?::storage_type_enum, ?, ?, 0.00, 0.00) " +
                     "RETURNING id";
        
        Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                request.unitCode(),
                request.location(),
                request.storageType().name(),
                request.totalMassCapacity(),
                request.totalVolumeCapacity()
        );

        StorageUnit saved = storageUnitRepository.findById(newId)
                .orElseThrow(() -> new StorageUnitNotFoundException("Failed to create storage unit"));

        return toResponseDTO(saved);
    }

    public StorageUnitResponseDTO updateStorageUnit(Long id, StorageUnitRequestDTO request) {
        StorageUnit storageUnit = storageUnitRepository.findById(id)
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + id));

        if (!storageUnit.getUnitCode().equals(request.unitCode()) && 
            storageUnitRepository.existsByUnitCode(request.unitCode())) {
            throw new StorageUnitAlreadyExistsException("Storage unit with code already exists: " + request.unitCode());
        }

        String sql = "UPDATE storage_unit SET " +
                     "unit_code = ?, " +
                     "location = ?, " +
                     "storage_type = ?::storage_type_enum, " +
                     "total_mass_capacity = ?, " +
                     "total_volume_capacity = ?, " +
                     "current_mass = ?, " +
                     "current_volume = ? " +
                     "WHERE id = ?";
        
        jdbcTemplate.update(sql,
                request.unitCode(),
                request.location(),
                request.storageType().name(),
                request.totalMassCapacity(),
                request.totalVolumeCapacity(),
                storageUnit.getCurrentMass(),
                storageUnit.getCurrentVolume(),
                id
        );

        storageUnit.setUnitCode(request.unitCode());
        storageUnit.setLocation(request.location());
        storageUnit.setStorageType(request.storageType());
        storageUnit.setTotalMassCapacity(request.totalMassCapacity());
        storageUnit.setTotalVolumeCapacity(request.totalVolumeCapacity());

        return toResponseDTO(storageUnit);
    }

    public PageResponseDTO<CargoStorageResponseDTO> getStorageUnitInventory(Long id, int page, int size) {
        if (!storageUnitRepository.existsById(id)) {
            throw new StorageUnitNotFoundException("Storage unit not found with id: " + id);
        }

        return cargoStorageService.getStorageUnitCargo(id, page, size);
    }

    public void updateStorageUnitCapacity(Long storageUnitId) {
        String sql = "UPDATE storage_unit su " +
                     "SET current_mass = ( " +
                     "    SELECT COALESCE(SUM(cs.quantity * c.mass_per_unit), 0.00) " +
                     "    FROM cargo_storage cs " +
                     "    JOIN cargo c ON cs.cargo_id = c.id " +
                     "    WHERE cs.storage_unit_id = su.id " +
                     "), " +
                     "current_volume = ( " +
                     "    SELECT COALESCE(SUM(cs.quantity * c.volume_per_unit), 0.00) " +
                     "    FROM cargo_storage cs " +
                     "    JOIN cargo c ON cs.cargo_id = c.id " +
                     "    WHERE cs.storage_unit_id = su.id " +
                     ") " +
                     "WHERE su.id = ?";
        
        jdbcTemplate.update(sql, storageUnitId);
    }

    public boolean hasAvailableCapacity(Long storageUnitId, BigDecimal requiredMass, BigDecimal requiredVolume) {
        String sql = "SELECT " +
                     "(total_mass_capacity - current_mass) >= ? AND " +
                     "(total_volume_capacity - current_volume) >= ? " +
                     "FROM storage_unit WHERE id = ?";
        
        return jdbcTemplate.queryForObject(sql, Boolean.class, requiredMass, requiredVolume, storageUnitId);
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

    public StorageUnit getEntityById(Long id) {
        return storageUnitRepository.findById(id)
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + id));
    }
}

