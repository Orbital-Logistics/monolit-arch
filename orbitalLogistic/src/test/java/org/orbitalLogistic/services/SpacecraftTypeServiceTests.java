package org.orbitalLogistic.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.dto.response.SpacecraftTypeResponseDTO;
import org.orbitalLogistic.entities.SpacecraftType;
import org.orbitalLogistic.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.exceptions.SpacecraftTypeNotFoundException;
import org.orbitalLogistic.mappers.SpacecraftTypeMapper;
import org.orbitalLogistic.repositories.SpacecraftTypeRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacecraftTypeServiceTests {

    @Mock
    private SpacecraftTypeRepository spacecraftTypeRepository;

    @Mock
    private SpacecraftTypeMapper spacecraftTypeMapper;

    @InjectMocks
    private SpacecraftTypeService spacecraftTypeService;

    private SpacecraftType testSpacecraftType;
    private SpacecraftTypeResponseDTO testResponseDTO;
    private SpacecraftTypeRequestDTO testRequestDTO;

    @BeforeEach
    void setUp() {
        testSpacecraftType = SpacecraftType.builder()
                .id(1L)
                .typeName("Cargo Ship")
                .classification(SpacecraftClassification.CARGO_HAULER)
                .maxCrewCapacity(10) 
                .build();

        testResponseDTO = new SpacecraftTypeResponseDTO(
                1L, "Cargo Ship", SpacecraftClassification.CARGO_HAULER, 10 
        );

        testRequestDTO = new SpacecraftTypeRequestDTO(
                "Cargo Ship", SpacecraftClassification.CARGO_HAULER, 10 
        );
    }

    @Test
    void getAllSpacecraftTypes_ShouldReturnListOfTypes() {
        
        List<SpacecraftType> types = List.of(testSpacecraftType);
        when(spacecraftTypeRepository.findAll()).thenReturn(types);
        when(spacecraftTypeMapper.toResponseDTO(testSpacecraftType)).thenReturn(testResponseDTO);

        
        List<SpacecraftTypeResponseDTO> result = spacecraftTypeService.getAllSpacecraftTypes();

        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cargo Ship", result.get(0).typeName());
        assertEquals(SpacecraftClassification.CARGO_HAULER, result.get(0).classification());
        verify(spacecraftTypeRepository, times(1)).findAll();
        verify(spacecraftTypeMapper, times(1)).toResponseDTO(testSpacecraftType);
    }

    @Test
    void getAllSpacecraftTypes_WhenNoTypes_ShouldReturnEmptyList() {
        
        when(spacecraftTypeRepository.findAll()).thenReturn(List.of());

        
        List<SpacecraftTypeResponseDTO> result = spacecraftTypeService.getAllSpacecraftTypes();

        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(spacecraftTypeRepository, times(1)).findAll();
        verify(spacecraftTypeMapper, never()).toResponseDTO(any());
    }

    @Test
    void getAllSpacecraftTypes_WithMultipleTypes_ShouldReturnAllTypes() {
        
        SpacecraftType type1 = SpacecraftType.builder()
                .id(1L)
                .typeName("Cargo Ship")
                .classification(SpacecraftClassification.CARGO_HAULER)
                .maxCrewCapacity(10)
                .build();

        SpacecraftType type2 = SpacecraftType.builder()
                .id(2L)
                .typeName("Personnel Ship")
                .classification(SpacecraftClassification.PERSONNEL_TRANSPORT)
                .maxCrewCapacity(20)
                .build();

        List<SpacecraftType> types = List.of(type1, type2);
        when(spacecraftTypeRepository.findAll()).thenReturn(types);

        SpacecraftTypeResponseDTO response1 = new SpacecraftTypeResponseDTO(
                1L, "Cargo Ship", SpacecraftClassification.CARGO_HAULER, 10
        );
        SpacecraftTypeResponseDTO response2 = new SpacecraftTypeResponseDTO(
                2L, "Personnel Ship", SpacecraftClassification.PERSONNEL_TRANSPORT, 20
        );

        when(spacecraftTypeMapper.toResponseDTO(type1)).thenReturn(response1);
        when(spacecraftTypeMapper.toResponseDTO(type2)).thenReturn(response2);

        
        List<SpacecraftTypeResponseDTO> result = spacecraftTypeService.getAllSpacecraftTypes();

        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cargo Ship", result.get(0).typeName());
        assertEquals("Personnel Ship", result.get(1).typeName());
        verify(spacecraftTypeRepository, times(1)).findAll();
        verify(spacecraftTypeMapper, times(1)).toResponseDTO(type1);
        verify(spacecraftTypeMapper, times(1)).toResponseDTO(type2);
    }

    @Test
    void getSpacecraftTypeById_WithValidId_ShouldReturnType() {
        
        when(spacecraftTypeRepository.findById(1L)).thenReturn(Optional.of(testSpacecraftType));
        when(spacecraftTypeMapper.toResponseDTO(testSpacecraftType)).thenReturn(testResponseDTO);

        
        SpacecraftTypeResponseDTO result = spacecraftTypeService.getSpacecraftTypeById(1L);

        
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Cargo Ship", result.typeName());
        assertEquals(SpacecraftClassification.CARGO_HAULER, result.classification());
        verify(spacecraftTypeRepository, times(1)).findById(1L);
        verify(spacecraftTypeMapper, times(1)).toResponseDTO(testSpacecraftType);
    }

    @Test
    void getSpacecraftTypeById_WithInvalidId_ShouldThrowException() {
        
        when(spacecraftTypeRepository.findById(999L)).thenReturn(Optional.empty());

        
        SpacecraftTypeNotFoundException exception = assertThrows(
                SpacecraftTypeNotFoundException.class,
                () -> spacecraftTypeService.getSpacecraftTypeById(999L)
        );

        assertEquals("Spacecraft type not found with id: 999", exception.getMessage());
        verify(spacecraftTypeRepository, times(1)).findById(999L);
        verify(spacecraftTypeMapper, never()).toResponseDTO(any());
    }

    @Test
    void createSpacecraftType_WithValidRequest_ShouldCreateType() {
        
        SpacecraftType newType = SpacecraftType.builder()
                .typeName("Cargo Ship")
                .classification(SpacecraftClassification.CARGO_HAULER)
                .maxCrewCapacity(10)
                .build();

        when(spacecraftTypeMapper.toEntity(testRequestDTO)).thenReturn(newType);
        when(spacecraftTypeRepository.save(newType)).thenReturn(testSpacecraftType);
        when(spacecraftTypeMapper.toResponseDTO(testSpacecraftType)).thenReturn(testResponseDTO);

        
        SpacecraftTypeResponseDTO result = spacecraftTypeService.createSpacecraftType(testRequestDTO);

        
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Cargo Ship", result.typeName());
        assertEquals(SpacecraftClassification.CARGO_HAULER, result.classification());
        verify(spacecraftTypeMapper, times(1)).toEntity(testRequestDTO);
        verify(spacecraftTypeRepository, times(1)).save(newType);
        verify(spacecraftTypeMapper, times(1)).toResponseDTO(testSpacecraftType);
    }

    @Test
    void createSpacecraftType_WithNullRequest_ShouldPropagateException() {
        
        when(spacecraftTypeMapper.toEntity(null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> spacecraftTypeService.createSpacecraftType(null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(spacecraftTypeMapper, times(1)).toEntity(null);
        verify(spacecraftTypeRepository, never()).save(any());
    }

    @Test
    void createSpacecraftType_WithMinimalData_ShouldCreateType() {
        
        SpacecraftTypeRequestDTO minimalRequest = new SpacecraftTypeRequestDTO(
                "Minimal Ship", SpacecraftClassification.CARGO_HAULER, 5
        );

        SpacecraftType minimalType = SpacecraftType.builder()
                .typeName("Minimal Ship")
                .classification(SpacecraftClassification.CARGO_HAULER)
                .maxCrewCapacity(5)
                .build();

        SpacecraftTypeResponseDTO minimalResponse = new SpacecraftTypeResponseDTO(
                1L, "Minimal Ship", SpacecraftClassification.CARGO_HAULER, 5
        );

        when(spacecraftTypeMapper.toEntity(minimalRequest)).thenReturn(minimalType);
        when(spacecraftTypeRepository.save(minimalType)).thenReturn(testSpacecraftType);
        when(spacecraftTypeMapper.toResponseDTO(testSpacecraftType)).thenReturn(minimalResponse);

        
        SpacecraftTypeResponseDTO result = spacecraftTypeService.createSpacecraftType(minimalRequest);

        
        assertNotNull(result);
        assertEquals("Minimal Ship", result.typeName());
        verify(spacecraftTypeMapper, times(1)).toEntity(minimalRequest);
        verify(spacecraftTypeRepository, times(1)).save(minimalType);
    }

    @Test
    void createSpacecraftType_WithAllFields_ShouldCreateType() {
        
        SpacecraftTypeRequestDTO fullRequest = new SpacecraftTypeRequestDTO(
                "Full Ship", SpacecraftClassification.PERSONNEL_TRANSPORT, 20
        );

        SpacecraftType fullType = SpacecraftType.builder()
                .typeName("Full Ship")
                .classification(SpacecraftClassification.PERSONNEL_TRANSPORT)
                .maxCrewCapacity(20)
                .build();

        SpacecraftTypeResponseDTO fullResponse = new SpacecraftTypeResponseDTO(
                1L, "Full Ship", SpacecraftClassification.PERSONNEL_TRANSPORT, 20
        );

        when(spacecraftTypeMapper.toEntity(fullRequest)).thenReturn(fullType);
        when(spacecraftTypeRepository.save(fullType)).thenReturn(testSpacecraftType);
        when(spacecraftTypeMapper.toResponseDTO(testSpacecraftType)).thenReturn(fullResponse);

        
        SpacecraftTypeResponseDTO result = spacecraftTypeService.createSpacecraftType(fullRequest);

        
        assertNotNull(result);
        assertEquals("Full Ship", result.typeName());
        assertEquals(SpacecraftClassification.PERSONNEL_TRANSPORT, result.classification());
        assertEquals(20, result.maxCrewCapacity()); 
        verify(spacecraftTypeMapper, times(1)).toEntity(fullRequest);
        verify(spacecraftTypeRepository, times(1)).save(fullType);
    }

    @Test
    void createSpacecraftType_WithRepositoryException_ShouldPropagateException() {
        
        when(spacecraftTypeMapper.toEntity(testRequestDTO)).thenReturn(testSpacecraftType);
        when(spacecraftTypeRepository.save(testSpacecraftType))
                .thenThrow(new RuntimeException("Database error"));

        
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> spacecraftTypeService.createSpacecraftType(testRequestDTO)
        );

        assertEquals("Database error", exception.getMessage());
        verify(spacecraftTypeMapper, times(1)).toEntity(testRequestDTO);
        verify(spacecraftTypeRepository, times(1)).save(testSpacecraftType);
        verify(spacecraftTypeMapper, never()).toResponseDTO(any());
    }
}
