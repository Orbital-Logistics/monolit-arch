package org.orbitalLogistic.services;

import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftResponseDTO;
import org.orbitalLogistic.entities.Spacecraft;
import org.orbitalLogistic.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.entities.SpacecraftType;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.exceptions.SpacecraftAlreadyExistsException;
import org.orbitalLogistic.exceptions.SpacecraftNotFoundException;
import org.orbitalLogistic.mappers.SpacecraftMapper;
import org.orbitalLogistic.repositories.SpacecraftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

@Service
@Validated
public class SpacecraftService {

    private final SpacecraftRepository spacecraftRepository;
    private final SpacecraftMapper spacecraftMapper;
    private final JdbcTemplate jdbcTemplate;

    private SpacecraftTypeService spacecraftTypeService;

    public SpacecraftService(SpacecraftRepository spacecraftRepository,
                            SpacecraftMapper spacecraftMapper,
                            JdbcTemplate jdbcTemplate) {
        this.spacecraftRepository = spacecraftRepository;
        this.spacecraftMapper = spacecraftMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    public void setSpacecraftTypeService(@Lazy SpacecraftTypeService spacecraftTypeService) {
        this.spacecraftTypeService = spacecraftTypeService;
    }

    public PageResponseDTO<SpacecraftResponseDTO> getSpacecrafts(String name, String status, int page, int size) {
        int offset = page * size;
        List<Spacecraft> spacecrafts = spacecraftRepository.findWithFilters(name, status, size, offset);
        long total = spacecraftRepository.countWithFilters(name, status);

        List<SpacecraftResponseDTO> spacecraftDTOs = spacecrafts.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(spacecraftDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public List<SpacecraftResponseDTO> getSpacecraftsScroll(int page, int size) {
        int offset = page * size;
        List<Spacecraft> spacecrafts = spacecraftRepository.findWithFilters(null, null, size + 1, offset);
        
        return spacecrafts.stream()
                .limit(size)
                .map(this::toResponseDTO)
                .toList();
    }

    public SpacecraftResponseDTO getSpacecraftById(Long id) {
        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));
        return toResponseDTO(spacecraft);
    }

    public SpacecraftResponseDTO createSpacecraft(SpacecraftRequestDTO request) {
        if (spacecraftRepository.existsByRegistryCode(request.registryCode())) {
            throw new SpacecraftAlreadyExistsException("Spacecraft with registry code already exists: " + request.registryCode());
        }

        spacecraftTypeService.getEntityById(request.spacecraftTypeId());

        String sql = "INSERT INTO spacecraft " +
                     "(registry_code, name, spacecraft_type_id, mass_capacity, volume_capacity, status, current_location) " +
                     "VALUES (?, ?, ?, ?, ?, ?::spacecraft_status_enum, ?) " +
                     "RETURNING id";
        
        Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                request.registryCode(),
                request.name(),
                request.spacecraftTypeId(),
                request.massCapacity(),
                request.volumeCapacity(),
                request.status().name(),
                request.currentLocation()
        );

        Spacecraft saved = spacecraftRepository.findById(newId)
                .orElseThrow(() -> new DataNotFoundException("Failed to create spacecraft"));

        return toResponseDTO(saved);
    }

    public SpacecraftResponseDTO updateSpacecraft(Long id, SpacecraftRequestDTO request) {
        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));

        if (!spacecraft.getRegistryCode().equals(request.registryCode()) && 
            spacecraftRepository.existsByRegistryCode(request.registryCode())) {
            throw new SpacecraftAlreadyExistsException("Spacecraft with registry code already exists: " + request.registryCode());
        }

        spacecraftTypeService.getEntityById(request.spacecraftTypeId());

        String sql = "UPDATE spacecraft SET " +
                     "registry_code = ?, " +
                     "name = ?, " +
                     "spacecraft_type_id = ?, " +
                     "mass_capacity = ?, " +
                     "volume_capacity = ?, " +
                     "status = ?::spacecraft_status_enum, " +
                     "current_location = ? " +
                     "WHERE id = ?";
        
        jdbcTemplate.update(sql,
                request.registryCode(),
                request.name(),
                request.spacecraftTypeId(),
                request.massCapacity(),
                request.volumeCapacity(),
                request.status().name(),
                request.currentLocation(),
                id
        );

        spacecraft.setRegistryCode(request.registryCode());
        spacecraft.setName(request.name());
        spacecraft.setSpacecraftTypeId(request.spacecraftTypeId());
        spacecraft.setMassCapacity(request.massCapacity());
        spacecraft.setVolumeCapacity(request.volumeCapacity());
        spacecraft.setStatus(request.status() != null ? request.status() : spacecraft.getStatus());
        spacecraft.setCurrentLocation(request.currentLocation());

        return toResponseDTO(spacecraft);
    }

    public void deleteSpacecraft(Long id) {
        if (!spacecraftRepository.existsById(id)) {
            throw new SpacecraftNotFoundException("Spacecraft not found with id: " + id);
        }
        spacecraftRepository.deleteById(id);
    }

    public List<SpacecraftResponseDTO> getAvailableSpacecrafts() {
        return spacecraftRepository.findAvailableForMission().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public SpacecraftResponseDTO updateSpacecraftStatus(Long id, SpacecraftStatus status) {
        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));

        String sql = "UPDATE spacecraft SET status = ?::spacecraft_status_enum WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), id);

        spacecraft.setStatus(status);
        return toResponseDTO(spacecraft);
    }

    public SpacecraftResponseDTO changeSpacecraftLocation(Long id, String newLocation) {
        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));

        String sql = "UPDATE spacecraft SET current_location = ? WHERE id = ?";
        jdbcTemplate.update(sql, newLocation, id);

        spacecraft.setCurrentLocation(newLocation);
        return toResponseDTO(spacecraft);
    }

    public SpacecraftResponseDTO putSpacecraftInMaintenance(Long id) {
        return updateSpacecraftStatus(id, SpacecraftStatus.MAINTENANCE);
    }

    public SpacecraftResponseDTO putSpacecraftInTransit(Long id) {
        return updateSpacecraftStatus(id, SpacecraftStatus.IN_TRANSIT);
    }

    public SpacecraftResponseDTO dockSpacecraft(Long id, String location) {
        SpacecraftResponseDTO response = changeSpacecraftLocation(id, location);
        return updateSpacecraftStatus(id, SpacecraftStatus.DOCKED);
    }

    public Spacecraft getEntityById(Long id) {
        return spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));
    }

    private SpacecraftResponseDTO toResponseDTO(Spacecraft spacecraft) {
        SpacecraftType spacecraftType = spacecraftTypeService.getEntityById(spacecraft.getSpacecraftTypeId());

        BigDecimal currentMassUsage = calculateCurrentMassUsage(spacecraft);
        BigDecimal currentVolumeUsage = calculateCurrentVolumeUsage(spacecraft);

        return spacecraftMapper.toResponseDTO(
            spacecraft,
            spacecraftType.getTypeName(),
            spacecraftType.getClassification(),
            currentMassUsage,
            currentVolumeUsage
        );
    }

    private BigDecimal calculateCurrentMassUsage(Spacecraft spacecraft) {
        String sql = "SELECT COALESCE(SUM(cm.quantity * c.mass_per_unit), 0) " +
                     "FROM cargo_manifest cm " +
                     "JOIN cargo c ON cm.cargo_id = c.id " +
                     "WHERE cm.spacecraft_id = ? AND cm.manifest_status IN ('LOADED', 'IN_TRANSIT')";
        
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, spacecraft.getId());
    }

    private BigDecimal calculateCurrentVolumeUsage(Spacecraft spacecraft) {
        String sql = "SELECT COALESCE(SUM(cm.quantity * c.volume_per_unit), 0) " +
                     "FROM cargo_manifest cm " +
                     "JOIN cargo c ON cm.cargo_id = c.id " +
                     "WHERE cm.spacecraft_id = ? AND cm.manifest_status IN ('LOADED', 'IN_TRANSIT')";
        
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, spacecraft.getId());
    }

    public BigDecimal getAvailableMassCapacity(Long spacecraftId) {
        Spacecraft spacecraft = spacecraftRepository.findById(spacecraftId)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found"));
        
        BigDecimal currentUsage = calculateCurrentMassUsage(spacecraft);
        return spacecraft.getMassCapacity().subtract(currentUsage);
    }

    public BigDecimal getAvailableVolumeCapacity(Long spacecraftId) {
        Spacecraft spacecraft = spacecraftRepository.findById(spacecraftId)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found"));
        
        BigDecimal currentUsage = calculateCurrentVolumeUsage(spacecraft);
        return spacecraft.getVolumeCapacity().subtract(currentUsage);
    }
}

