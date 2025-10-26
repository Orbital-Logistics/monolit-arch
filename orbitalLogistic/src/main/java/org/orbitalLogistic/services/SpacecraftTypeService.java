package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftTypeResponseDTO;
import org.orbitalLogistic.entities.SpacecraftType;
import org.orbitalLogistic.exceptions.SpacecraftTypeNotFoundException;
import org.orbitalLogistic.mappers.SpacecraftTypeMapper;
import org.orbitalLogistic.repositories.SpacecraftTypeRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpacecraftTypeService {

    private final SpacecraftTypeRepository spacecraftTypeRepository;
    private final SpacecraftTypeMapper spacecraftTypeMapper;
    private final JdbcTemplate jdbcTemplate;

    public List<SpacecraftTypeResponseDTO> getAllSpacecraftTypes() {
        List<SpacecraftType> types = (List<SpacecraftType>) spacecraftTypeRepository.findAll();
        return types.stream()
                .map(spacecraftTypeMapper::toResponseDTO)
                .toList();
    }

    public SpacecraftTypeResponseDTO getSpacecraftTypeById(Long id) {
        SpacecraftType type = spacecraftTypeRepository.findById(id)
                .orElseThrow(() -> new SpacecraftTypeNotFoundException("Spacecraft type not found with id: " + id));
        return spacecraftTypeMapper.toResponseDTO(type);
    }

    public SpacecraftTypeResponseDTO createSpacecraftType(SpacecraftTypeRequestDTO request) {
        String sql = "INSERT INTO spacecraft_type " +
                     "(type_name, classification, max_crew_capacity) " +
                     "VALUES (?, ?::spacecraft_classification_enum, ?) " +
                     "RETURNING id";
        
        Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                request.typeName(),
                request.classification().name(),
                request.maxCrewCapacity()
        );

        SpacecraftType saved = spacecraftTypeRepository.findById(newId)
                .orElseThrow(() -> new SpacecraftTypeNotFoundException("Failed to create spacecraft type"));

        return spacecraftTypeMapper.toResponseDTO(saved);
    }
}
