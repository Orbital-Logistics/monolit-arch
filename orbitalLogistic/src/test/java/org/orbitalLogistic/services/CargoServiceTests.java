package org.orbitalLogistic.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoRequestDTO;
import org.orbitalLogistic.dto.response.CargoResponseDTO;
import org.orbitalLogistic.entities.Cargo;
import org.orbitalLogistic.entities.CargoCategory;
import org.orbitalLogistic.entities.enums.CargoType;
import org.orbitalLogistic.entities.enums.HazardLevel;
import org.orbitalLogistic.exceptions.CargoAlreadyExistsException;
import org.orbitalLogistic.exceptions.CargoNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.CargoMapper;
import org.orbitalLogistic.repositories.CargoRepository;
import org.orbitalLogistic.repositories.CargoCategoryRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoServiceTests {

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private CargoCategoryRepository cargoCategoryRepository;

    @Mock
    private CargoMapper cargoMapper;

    @InjectMocks
    private CargoService cargoService;

    private Cargo testCargo;
    private CargoCategory testCategory;
    private CargoResponseDTO testResponseDTO;
    private CargoRequestDTO testRequestDTO;

    @BeforeEach
    void setUp() {
        testCategory = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .build();

        testCargo = Cargo.builder()
                .id(1L)
                .name("Scientific Equipment")
                .cargoCategoryId(1L)
                .massPerUnit(BigDecimal.valueOf(10.5))
                .volumePerUnit(BigDecimal.valueOf(5.0))
                .cargoType(CargoType.SCIENTIFIC)
                .hazardLevel(HazardLevel.LOW)
                .build();

        testResponseDTO = new CargoResponseDTO(
                1L, "Scientific Equipment", "Electronics",
                BigDecimal.valueOf(10.5), BigDecimal.valueOf(5.0),
                CargoType.SCIENTIFIC, HazardLevel.LOW, 0
        );

        testRequestDTO = new CargoRequestDTO(
                "Scientific Equipment", 1L,
                BigDecimal.valueOf(10.5), BigDecimal.valueOf(5.0),
                CargoType.SCIENTIFIC, HazardLevel.LOW
        );
    }

    @Test
    void getCargosScroll_WithValidParameters_ShouldReturnList() {
        // given
        List<Cargo> cargos = List.of(testCargo);
        when(cargoRepository.findWithFilters(null, null, null, 21, 0)).thenReturn(cargos);
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoMapper.toResponseDTO(any(Cargo.class), eq("Electronics"), eq(0)))
                .thenReturn(testResponseDTO);

        // when
        List<CargoResponseDTO> result = cargoService.getCargosScroll(0, 20);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Scientific Equipment", result.get(0).name());
        verify(cargoRepository, times(1)).findWithFilters(null, null, null, 21, 0);
    }

    @Test
    void getCargosScroll_WithMoreResultsThanSize_ShouldLimitResults() {
        // given
        Cargo cargo2 = Cargo.builder()
                .id(2L)
                .name("Equipment 2")
                .cargoCategoryId(1L)
                .massPerUnit(BigDecimal.valueOf(15.0))
                .volumePerUnit(BigDecimal.valueOf(7.0))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.MEDIUM)
                .build();

        List<Cargo> cargos = List.of(testCargo, cargo2);

        // Используем eq() для всех аргументов или any() для всех
        when(cargoRepository.findWithFilters(isNull(), isNull(), isNull(), eq(2), eq(0)))
                .thenReturn(cargos);
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoMapper.toResponseDTO(any(Cargo.class), eq("Electronics"), eq(0)))
                .thenReturn(testResponseDTO);

        // when
        List<CargoResponseDTO> result = cargoService.getCargosScroll(0, 1);

        // then
        assertNotNull(result);
        assertEquals(1, result.size()); // Should be limited to size parameter
        verify(cargoRepository, times(1)).findWithFilters(null, null, null, 2, 0);
    }

    @Test
    void getCargosPaged_WithValidFilters_ShouldReturnPageResponse() {
        // given
        List<Cargo> cargos = List.of(testCargo);
        when(cargoRepository.findWithFilters("Scientific", "SCIENTIFIC", "LOW", 20, 0))
                .thenReturn(cargos);
        when(cargoRepository.countWithFilters("Scientific", "SCIENTIFIC", "LOW"))
                .thenReturn(1L);
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoMapper.toResponseDTO(any(Cargo.class), eq("Electronics"), eq(0)))
                .thenReturn(testResponseDTO);

        // when
        PageResponseDTO<CargoResponseDTO> result = cargoService.getCargosPaged("Scientific", "SCIENTIFIC", "LOW", 0, 20);

        // then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(20, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.first());
        assertTrue(result.last());
        verify(cargoRepository, times(1)).findWithFilters("Scientific", "SCIENTIFIC", "LOW", 20, 0);
        verify(cargoRepository, times(1)).countWithFilters("Scientific", "SCIENTIFIC", "LOW");
    }

    @Test
    void getCargosPaged_WithNullFilters_ShouldReturnAllCargos() {
        // given
        List<Cargo> cargos = List.of(testCargo);
        when(cargoRepository.findWithFilters(null, null, null, 20, 0))
                .thenReturn(cargos);
        when(cargoRepository.countWithFilters(null, null, null))
                .thenReturn(1L);
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoMapper.toResponseDTO(any(Cargo.class), eq("Electronics"), eq(0)))
                .thenReturn(testResponseDTO);

        // when
        PageResponseDTO<CargoResponseDTO> result = cargoService.getCargosPaged(null, null, null, 0, 20);

        // then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(cargoRepository, times(1)).findWithFilters(null, null, null, 20, 0);
    }

    @Test
    void getCargoById_WithValidId_ShouldReturnCargo() {
        // given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoMapper.toResponseDTO(any(Cargo.class), eq("Electronics"), eq(0)))
                .thenReturn(testResponseDTO);

        // when
        CargoResponseDTO result = cargoService.getCargoById(1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Scientific Equipment", result.name());
        verify(cargoRepository, times(1)).findById(1L);
    }

    @Test
    void getCargoById_WithInvalidId_ShouldThrowException() {
        // given
        when(cargoRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        CargoNotFoundException exception = assertThrows(
                CargoNotFoundException.class,
                () -> cargoService.getCargoById(999L)
        );

        assertEquals("Cargo not found with id: 999", exception.getMessage());
        verify(cargoRepository, times(1)).findById(999L);
    }

    @Test
    void createCargo_WithValidRequest_ShouldCreateCargo() {
        // given
        Cargo newCargo = Cargo.builder()
                .name("Scientific Equipment")
                .cargoCategoryId(1L)
                .massPerUnit(BigDecimal.valueOf(10.5))
                .volumePerUnit(BigDecimal.valueOf(5.0))
                .cargoType(CargoType.SCIENTIFIC)
                .hazardLevel(HazardLevel.LOW)
                .build();

        when(cargoRepository.existsByName("Scientific Equipment")).thenReturn(false);
        // Указываем, что метод может быть вызван несколько раз
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoMapper.toEntity(testRequestDTO)).thenReturn(newCargo);
        when(cargoRepository.save(newCargo)).thenReturn(testCargo);
        when(cargoMapper.toResponseDTO(any(Cargo.class), eq("Electronics"), eq(0)))
                .thenReturn(testResponseDTO);

        // when
        CargoResponseDTO result = cargoService.createCargo(testRequestDTO);

        // then
        assertNotNull(result);
        assertEquals("Scientific Equipment", result.name());
        verify(cargoRepository, times(1)).existsByName("Scientific Equipment");
        // Ожидаем 2 вызова, так как метод вызывается и в createCargo и в toResponseDTO
        verify(cargoCategoryRepository, times(2)).findById(1L);
        verify(cargoMapper, times(1)).toEntity(testRequestDTO);
        verify(cargoRepository, times(1)).save(newCargo);
    }

    @Test
    void createCargo_WithExistingName_ShouldThrowException() {
        // given
        when(cargoRepository.existsByName("Scientific Equipment")).thenReturn(true);

        // when & then
        CargoAlreadyExistsException exception = assertThrows(
                CargoAlreadyExistsException.class,
                () -> cargoService.createCargo(testRequestDTO)
        );

        assertEquals("Cargo with name already exists: Scientific Equipment", exception.getMessage());
        verify(cargoRepository, times(1)).existsByName("Scientific Equipment");
        verify(cargoRepository, never()).save(any());
    }

    @Test
    void createCargo_WithInvalidCategoryId_ShouldThrowException() {
        // given
        when(cargoRepository.existsByName("Scientific Equipment")).thenReturn(false);
        when(cargoCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        CargoRequestDTO requestWithInvalidCategory = new CargoRequestDTO(
                "Scientific Equipment", 999L,
                BigDecimal.valueOf(10.5), BigDecimal.valueOf(5.0),
                CargoType.SCIENTIFIC, HazardLevel.LOW
        );

        // when & then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> cargoService.createCargo(requestWithInvalidCategory)
        );

        assertEquals("Cargo category not found", exception.getMessage());
        verify(cargoCategoryRepository, times(1)).findById(999L);
        verify(cargoRepository, never()).save(any());
    }

    @Test
    void updateCargo_WithValidId_ShouldUpdateCargo() {
        // given
        CargoRequestDTO updateRequest = new CargoRequestDTO(
                "Updated Equipment", 1L,
                BigDecimal.valueOf(15.0), BigDecimal.valueOf(7.0),
                CargoType.EQUIPMENT, HazardLevel.MEDIUM
        );

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(cargoRepository.existsByName("Updated Equipment")).thenReturn(false);
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoRepository.save(any(Cargo.class))).thenReturn(testCargo);
        when(cargoMapper.toResponseDTO(any(Cargo.class), eq("Electronics"), eq(0)))
                .thenReturn(testResponseDTO);

        // when
        CargoResponseDTO result = cargoService.updateCargo(1L, updateRequest);

        // then
        assertNotNull(result);
        verify(cargoRepository, times(1)).findById(1L);
        verify(cargoRepository, times(1)).existsByName("Updated Equipment");
        verify(cargoRepository, times(1)).save(any(Cargo.class));
    }

    @Test
    void updateCargo_WithInvalidId_ShouldThrowException() {
        // given
        when(cargoRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        CargoNotFoundException exception = assertThrows(
                CargoNotFoundException.class,
                () -> cargoService.updateCargo(999L, testRequestDTO)
        );

        assertEquals("Cargo not found with id: 999", exception.getMessage());
        verify(cargoRepository, times(1)).findById(999L);
        verify(cargoRepository, never()).save(any());
    }

    @Test
    void updateCargo_WithExistingName_ShouldThrowException() {
        // given
        CargoRequestDTO updateRequest = new CargoRequestDTO(
                "Existing Name", 1L,
                BigDecimal.valueOf(10.5), BigDecimal.valueOf(5.0),
                CargoType.EQUIPMENT, HazardLevel.LOW
        );

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(cargoRepository.existsByName("Existing Name")).thenReturn(true);

        // when & then
        CargoAlreadyExistsException exception = assertThrows(
                CargoAlreadyExistsException.class,
                () -> cargoService.updateCargo(1L, updateRequest)
        );

        assertEquals("Cargo with name already exists: Existing Name", exception.getMessage());
        verify(cargoRepository, times(1)).existsByName("Existing Name");
        verify(cargoRepository, never()).save(any());
    }

    @Test
    void updateCargo_WithSameName_ShouldNotThrowException() {
        // given
        CargoRequestDTO updateRequest = new CargoRequestDTO(
                "Scientific Equipment", 1L,
                BigDecimal.valueOf(15.0), BigDecimal.valueOf(7.0),
                CargoType.SCIENTIFIC, HazardLevel.MEDIUM
        );

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoRepository.save(any(Cargo.class))).thenReturn(testCargo);
        when(cargoMapper.toResponseDTO(any(Cargo.class), eq("Electronics"), eq(0)))
                .thenReturn(testResponseDTO);

        // when
        CargoResponseDTO result = cargoService.updateCargo(1L, updateRequest);

        // then
        assertNotNull(result);
        verify(cargoRepository, never()).existsByName(anyString());
        verify(cargoRepository, times(1)).save(any(Cargo.class));
    }

    @Test
    void deleteCargo_WithValidId_ShouldDeleteCargo() {
        // given
        when(cargoRepository.existsById(1L)).thenReturn(true);

        // when
        cargoService.deleteCargo(1L);

        // then
        verify(cargoRepository, times(1)).existsById(1L);
        verify(cargoRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCargo_WithInvalidId_ShouldThrowException() {
        // given
        when(cargoRepository.existsById(999L)).thenReturn(false);

        // when & then
        CargoNotFoundException exception = assertThrows(
                CargoNotFoundException.class,
                () -> cargoService.deleteCargo(999L)
        );

        assertEquals("Cargo not found with id: 999", exception.getMessage());
        verify(cargoRepository, times(1)).existsById(999L);
        verify(cargoRepository, never()).deleteById(any());
    }

    @Test
    void searchCargos_ShouldCallGetCargosPaged() {
        // given
        List<Cargo> cargos = List.of(testCargo);
        when(cargoRepository.findWithFilters("test", "SCIENTIFIC", "LOW", 20, 0))
                .thenReturn(cargos);
        when(cargoRepository.countWithFilters("test", "SCIENTIFIC", "LOW"))
                .thenReturn(1L);
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoMapper.toResponseDTO(any(Cargo.class), eq("Electronics"), eq(0)))
                .thenReturn(testResponseDTO);

        // when
        PageResponseDTO<CargoResponseDTO> result = cargoService.searchCargos("test", "SCIENTIFIC", "LOW", 0, 20);

        // then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(cargoRepository, times(1)).findWithFilters("test", "SCIENTIFIC", "LOW", 20, 0);
    }

    @Test
    void toResponseDTO_WithValidCargo_ShouldReturnResponseDTO() {
        // given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoMapper.toResponseDTO(any(Cargo.class), eq("Electronics"), eq(0)))
                .thenReturn(testResponseDTO);

        // when
        CargoResponseDTO result = cargoService.getCargoById(1L);

        // then
        assertNotNull(result);
        assertEquals("Electronics", result.cargoCategoryName());
        verify(cargoCategoryRepository, times(1)).findById(1L);
    }

    @Test
    void toResponseDTO_WithInvalidCategory_ShouldThrowException() {
        // given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> cargoService.getCargoById(1L)
        );

        assertEquals("Cargo category not found", exception.getMessage());
        verify(cargoCategoryRepository, times(1)).findById(1L);
    }
}
