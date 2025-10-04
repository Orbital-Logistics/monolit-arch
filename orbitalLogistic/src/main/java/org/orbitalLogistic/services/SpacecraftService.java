package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
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
import org.orbitalLogistic.repositories.SpacecraftTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SpacecraftService {

    private final SpacecraftRepository spacecraftRepository;
    private final SpacecraftTypeRepository spacecraftTypeRepository;
    private final SpacecraftMapper spacecraftMapper;

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<SpacecraftResponseDTO> getSpacecraftsScroll(int page, int size) {
        int offset = page * size;
        List<Spacecraft> spacecrafts = spacecraftRepository.findWithFilters(null, null, size + 1, offset);
        
        return spacecrafts.stream()
                .limit(size)
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpacecraftResponseDTO getSpacecraftById(Long id) {
        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));
        return toResponseDTO(spacecraft);
    }

    public SpacecraftResponseDTO createSpacecraft(SpacecraftRequestDTO request) {
        if (spacecraftRepository.existsByRegistryCode(request.registryCode())) {
            throw new SpacecraftAlreadyExistsException("Spacecraft with registry code already exists: " + request.registryCode());
        }

        SpacecraftType spacecraftType = spacecraftTypeRepository.findById(request.spacecraftTypeId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft type not found"));

        Spacecraft spacecraft = spacecraftMapper.toEntity(request);
        spacecraft.setSpacecraftTypeId(spacecraftType.getId());

        Spacecraft saved = spacecraftRepository.save(spacecraft);
        return toResponseDTO(saved);
    }

    public SpacecraftResponseDTO updateSpacecraft(Long id, SpacecraftRequestDTO request) {
        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));

        if (!spacecraft.getRegistryCode().equals(request.registryCode()) && 
            spacecraftRepository.existsByRegistryCode(request.registryCode())) {
            throw new SpacecraftAlreadyExistsException("Spacecraft with registry code already exists: " + request.registryCode());
        }

        SpacecraftType spacecraftType = spacecraftTypeRepository.findById(request.spacecraftTypeId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft type not found"));

        spacecraft.setRegistryCode(request.registryCode());
        spacecraft.setName(request.name());
        spacecraft.setSpacecraftTypeId(spacecraftType.getId());
        spacecraft.setMassCapacity(request.massCapacity());
        spacecraft.setVolumeCapacity(request.volumeCapacity());
        spacecraft.setStatus(request.status() != null ? request.status() : spacecraft.getStatus());
        spacecraft.setCurrentLocation(request.currentLocation());

        Spacecraft updated = spacecraftRepository.save(spacecraft);
        return toResponseDTO(updated);
    }

    public void deleteSpacecraft(Long id) {
        if (!spacecraftRepository.existsById(id)) {
            throw new SpacecraftNotFoundException("Spacecraft not found with id: " + id);
        }
        spacecraftRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<SpacecraftResponseDTO> getAvailableSpacecrafts() {
        return spacecraftRepository.findAvailableForMission().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public SpacecraftResponseDTO updateSpacecraftStatus(Long id, SpacecraftStatus status) {
        Spacecraft spacecraft = spacecraftRepository.findById(id)
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + id));

        spacecraft.setStatus(status);
        Spacecraft updated = spacecraftRepository.save(spacecraft);
        return toResponseDTO(updated);
    }

    private SpacecraftResponseDTO toResponseDTO(Spacecraft spacecraft) {
        SpacecraftType spacecraftType = spacecraftTypeRepository.findById(spacecraft.getSpacecraftTypeId())
                .orElseThrow(() -> new DataNotFoundException("Spacecraft type not found"));

        // Calculate current usage (simplified - you might want to implement actual logic)
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
        // TODO: Implement actual calculation from cargo manifests
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateCurrentVolumeUsage(Spacecraft spacecraft) {
        // TODO: Implement actual calculation from cargo manifests
        return BigDecimal.ZERO;
    }
}
