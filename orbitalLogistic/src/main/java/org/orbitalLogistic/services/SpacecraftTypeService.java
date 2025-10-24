package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftTypeResponseDTO;
import org.orbitalLogistic.entities.SpacecraftType;
import org.orbitalLogistic.exceptions.SpacecraftTypeNotFoundException;
import org.orbitalLogistic.mappers.SpacecraftTypeMapper;
import org.orbitalLogistic.repositories.SpacecraftTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SpacecraftTypeService {

    private final SpacecraftTypeRepository spacecraftTypeRepository;
    private final SpacecraftTypeMapper spacecraftTypeMapper;

    @Transactional(readOnly = true)
    public List<SpacecraftTypeResponseDTO> getAllSpacecraftTypes() {
        List<SpacecraftType> types = (List<SpacecraftType>) spacecraftTypeRepository.findAll();
        return types.stream()
                .map(spacecraftTypeMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpacecraftTypeResponseDTO getSpacecraftTypeById(Long id) {
        SpacecraftType type = spacecraftTypeRepository.findById(id)
                .orElseThrow(() -> new SpacecraftTypeNotFoundException("Spacecraft type not found with id: " + id));
        return spacecraftTypeMapper.toResponseDTO(type);
    }

    public SpacecraftTypeResponseDTO createSpacecraftType(SpacecraftTypeRequestDTO request) {
        SpacecraftType type = spacecraftTypeMapper.toEntity(request);
        SpacecraftType saved = spacecraftTypeRepository.save(type);
        return spacecraftTypeMapper.toResponseDTO(saved);
    }
}
