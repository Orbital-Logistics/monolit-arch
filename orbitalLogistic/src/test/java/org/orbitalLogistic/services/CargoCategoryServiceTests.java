package org.orbitalLogistic.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.request.CargoCategoryRequestDTO;
import org.orbitalLogistic.dto.response.CargoCategoryResponseDTO;
import org.orbitalLogistic.entities.CargoCategory;
import org.orbitalLogistic.exceptions.CargoCategoryNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.CargoCategoryMapper;
import org.orbitalLogistic.repositories.CargoCategoryRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoCategoryServiceTests {

    @Mock
    private CargoCategoryRepository cargoCategoryRepository;

    @Mock
    private CargoCategoryMapper cargoCategoryMapper;

    @InjectMocks
    private CargoCategoryService cargoCategoryService;

    private CargoCategory testCategory;
    private CargoCategoryResponseDTO testResponseDTO;
    private CargoCategoryRequestDTO testRequestDTO;

    @BeforeEach
    void setUp() {
        testCategory = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .parentCategoryId(null)
                .build();

        testResponseDTO = new CargoCategoryResponseDTO(
                1L, "Electronics", null, null, "Electronic devices", List.of(), 0
        );

        testRequestDTO = new CargoCategoryRequestDTO(
                "Electronics", null, "Electronic devices"
        );
    }

    @Test
    void getAllCategories_ShouldReturnListOfCategories() {
        // given
        List<CargoCategory> categories = List.of(testCategory);
        when(cargoCategoryRepository.findAll()).thenReturn(categories);
        when(cargoCategoryMapper.toResponseDTO(any(CargoCategory.class), any(), any(), anyInt()))
                .thenReturn(testResponseDTO);

        // when
        List<CargoCategoryResponseDTO> result = cargoCategoryService.getAllCategories();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).name());
        verify(cargoCategoryRepository, times(1)).findAll();
    }

    @Test
    void getAllCategories_WhenNoCategories_ShouldReturnEmptyList() {
        // given
        when(cargoCategoryRepository.findAll()).thenReturn(List.of());

        // when
        List<CargoCategoryResponseDTO> result = cargoCategoryService.getAllCategories();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cargoCategoryRepository, times(1)).findAll();
    }

    @Test
    void getCategoryById_WithValidId_ShouldReturnCategory() {
        // given
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoCategoryMapper.toResponseDTO(any(CargoCategory.class), any(), any(), anyInt()))
                .thenReturn(testResponseDTO);

        // when
        CargoCategoryResponseDTO result = cargoCategoryService.getCategoryById(1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Electronics", result.name());
        verify(cargoCategoryRepository, times(1)).findById(1L);
    }

    @Test
    void getCategoryById_WithInvalidId_ShouldThrowCargoCategoryNotFoundException() {
        // given
        when(cargoCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        CargoCategoryNotFoundException exception = assertThrows(
                CargoCategoryNotFoundException.class,
                () -> cargoCategoryService.getCategoryById(999L)
        );

        assertEquals("Cargo category not found with id: 999", exception.getMessage());
        verify(cargoCategoryRepository, times(1)).findById(999L);
    }

    @Test
    void getCategoryTree_ShouldReturnRootCategoriesWithChildren() {
        // given
        CargoCategory rootCategory = CargoCategory.builder()
                .id(1L)
                .name("Root")
                .parentCategoryId(null)
                .build();

        CargoCategory childCategory = CargoCategory.builder()
                .id(2L)
                .name("Child")
                .parentCategoryId(1L)
                .build();

        CargoCategoryResponseDTO childResponseDTO = new CargoCategoryResponseDTO(
                2L, "Child", 1L, "Root", "Child category", List.of(), 1
        );

        CargoCategoryResponseDTO rootResponseDTO = new CargoCategoryResponseDTO(
                1L, "Root", null, null, "Root category", List.of(childResponseDTO), 0
        );

        when(cargoCategoryRepository.findByParentCategoryIdIsNull()).thenReturn(List.of(rootCategory));
        when(cargoCategoryRepository.findByParentCategoryId(1L)).thenReturn(List.of(childCategory));

        // Используем lenient() чтобы избежать PotentialStubbingProblem
        lenient().when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));

        // Более гибкие моки для mapper
        when(cargoCategoryMapper.toResponseDTO(eq(rootCategory), isNull(), anyList(), eq(0)))
                .thenReturn(rootResponseDTO);
        when(cargoCategoryMapper.toResponseDTO(eq(childCategory), eq("Root"), anyList(), eq(1)))
                .thenReturn(childResponseDTO);

        // when
        List<CargoCategoryResponseDTO> result = cargoCategoryService.getCategoryTree();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Root", result.get(0).name());
        verify(cargoCategoryRepository, times(1)).findByParentCategoryIdIsNull();
        verify(cargoCategoryRepository, times(1)).findByParentCategoryId(1L);
    }

    @Test
    void getCategoryTree_WithMultipleRoots_ShouldReturnAllRootCategories() {
        // given
        CargoCategory root1 = CargoCategory.builder().id(1L).name("Root1").parentCategoryId(null).build();
        CargoCategory root2 = CargoCategory.builder().id(2L).name("Root2").parentCategoryId(null).build();

        CargoCategoryResponseDTO root1Response = new CargoCategoryResponseDTO(1L, "Root1", null, null, null, List.of(), 0);
        CargoCategoryResponseDTO root2Response = new CargoCategoryResponseDTO(2L, "Root2", null, null, null, List.of(), 0);

        when(cargoCategoryRepository.findByParentCategoryIdIsNull()).thenReturn(List.of(root1, root2));
        when(cargoCategoryRepository.findByParentCategoryId(anyLong())).thenReturn(List.of());

        // Гибкие моки для разных вызовов
        when(cargoCategoryMapper.toResponseDTO(eq(root1), isNull(), eq(List.of()), eq(0)))
                .thenReturn(root1Response);
        when(cargoCategoryMapper.toResponseDTO(eq(root2), isNull(), eq(List.of()), eq(0)))
                .thenReturn(root2Response);

        // when
        List<CargoCategoryResponseDTO> result = cargoCategoryService.getCategoryTree();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cargoCategoryRepository, times(1)).findByParentCategoryIdIsNull();
    }

    @Test
    void createCategory_WithValidRequest_ShouldCreateCategory() {
        // given
        when(cargoCategoryMapper.toEntity(testRequestDTO)).thenReturn(testCategory);
        when(cargoCategoryRepository.save(testCategory)).thenReturn(testCategory);
        when(cargoCategoryMapper.toResponseDTO(any(CargoCategory.class), any(), any(), anyInt()))
                .thenReturn(testResponseDTO);

        // when
        CargoCategoryResponseDTO result = cargoCategoryService.createCategory(testRequestDTO);

        // then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Electronics", result.name());
        verify(cargoCategoryRepository, times(1)).save(testCategory);
        verify(cargoCategoryMapper, times(1)).toEntity(testRequestDTO);
    }

    @Test
    void createCategory_WithValidParentId_ShouldCreateCategory() {
        // given
        CargoCategoryRequestDTO requestWithParent = new CargoCategoryRequestDTO(
                "Laptop", 1L, "Portable computers"
        );

        CargoCategory parentCategory = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .build();

        CargoCategory newCategory = CargoCategory.builder()
                .id(2L)
                .name("Laptop")
                .parentCategoryId(1L)
                .description("Portable computers")
                .build();

        CargoCategoryResponseDTO responseWithParent = new CargoCategoryResponseDTO(
                2L, "Laptop", 1L, "Electronics", "Portable computers", List.of(), 0
        );

        // Используем lenient() для дополнительных вызовов findById
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        lenient().when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));

        when(cargoCategoryMapper.toEntity(requestWithParent)).thenReturn(newCategory);
        when(cargoCategoryRepository.save(newCategory)).thenReturn(newCategory);
        when(cargoCategoryMapper.toResponseDTO(any(CargoCategory.class), any(), any(), anyInt()))
                .thenReturn(responseWithParent);

        // when
        CargoCategoryResponseDTO result = cargoCategoryService.createCategory(requestWithParent);

        // then
        assertNotNull(result);
        assertEquals(1L, result.parentCategoryId());
        assertEquals("Electronics", result.parentCategoryName());
        // Проверяем что findById вызывался, но не ограничиваем точное количество
        verify(cargoCategoryRepository, atLeastOnce()).findById(1L);
        verify(cargoCategoryRepository, times(1)).save(newCategory);
    }

    @Test
    void createCategory_WithInvalidParentId_ShouldThrowDataNotFoundException() {
        // given
        CargoCategoryRequestDTO requestWithInvalidParent = new CargoCategoryRequestDTO(
                "Laptop", 999L, "Portable computers"
        );

        when(cargoCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> cargoCategoryService.createCategory(requestWithInvalidParent)
        );

        assertEquals("Parent category not found", exception.getMessage());
        verify(cargoCategoryRepository, times(1)).findById(999L);
        verify(cargoCategoryRepository, never()).save(any());
    }

    @Test
    void createCategory_WithNullParentId_ShouldCreateRootCategory() {
        // given
        CargoCategoryRequestDTO requestWithNullParent = new CargoCategoryRequestDTO(
                "Root Category", null, "Root level category"
        );

        CargoCategory rootCategory = CargoCategory.builder()
                .id(1L)
                .name("Root Category")
                .parentCategoryId(null)
                .description("Root level category")
                .build();

        CargoCategoryResponseDTO rootResponse = new CargoCategoryResponseDTO(
                1L, "Root Category", null, null, "Root level category", List.of(), 0
        );

        when(cargoCategoryMapper.toEntity(requestWithNullParent)).thenReturn(rootCategory);
        when(cargoCategoryRepository.save(rootCategory)).thenReturn(rootCategory);
        when(cargoCategoryMapper.toResponseDTO(any(CargoCategory.class), any(), any(), anyInt()))
                .thenReturn(rootResponse);

        // when
        CargoCategoryResponseDTO result = cargoCategoryService.createCategory(requestWithNullParent);

        // then
        assertNotNull(result);
        assertNull(result.parentCategoryId());
        assertNull(result.parentCategoryName());
        verify(cargoCategoryRepository, never()).findById(any());
        verify(cargoCategoryRepository, times(1)).save(rootCategory);
    }

    @Test
    void buildCategoryTree_WithDeepNesting_ShouldBuildCompleteTree() {
        // given
        CargoCategory root = CargoCategory.builder().id(1L).name("Root").parentCategoryId(null).build();
        CargoCategory child = CargoCategory.builder().id(2L).name("Child").parentCategoryId(1L).build();
        CargoCategory grandchild = CargoCategory.builder().id(3L).name("Grandchild").parentCategoryId(2L).build();

        when(cargoCategoryRepository.findByParentCategoryIdIsNull()).thenReturn(List.of(root));
        when(cargoCategoryRepository.findByParentCategoryId(1L)).thenReturn(List.of(child));
        when(cargoCategoryRepository.findByParentCategoryId(2L)).thenReturn(List.of(grandchild));
        when(cargoCategoryRepository.findByParentCategoryId(3L)).thenReturn(List.of());

        // Моки для parent category lookups
        lenient().when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.empty());
        lenient().when(cargoCategoryRepository.findById(2L)).thenReturn(Optional.of(root));
        lenient().when(cargoCategoryRepository.findById(3L)).thenReturn(Optional.of(child));

        // Гибкий мок для mapper
        when(cargoCategoryMapper.toResponseDTO(any(CargoCategory.class), any(), any(), anyInt()))
                .thenAnswer(invocation -> {
                    CargoCategory category = invocation.getArgument(0);
                    String parentName = invocation.getArgument(1);
                    List<CargoCategoryResponseDTO> children = invocation.getArgument(2);
                    int level = invocation.getArgument(3);
                    return new CargoCategoryResponseDTO(
                            category.getId(), category.getName(), category.getParentCategoryId(),
                            parentName, category.getDescription(), children, level
                    );
                });

        // when
        List<CargoCategoryResponseDTO> result = cargoCategoryService.getCategoryTree();

        // then
        assertNotNull(result);
        // Проверяем что методы вызывались нужное количество раз
        verify(cargoCategoryRepository, times(1)).findByParentCategoryId(1L);
        verify(cargoCategoryRepository, times(1)).findByParentCategoryId(2L);
        verify(cargoCategoryRepository, times(1)).findByParentCategoryId(3L);
    }

    @Test
    void createCategory_WithEmptyDescription_ShouldCreateCategory() {
        // given
        CargoCategoryRequestDTO requestWithEmptyDescription = new CargoCategoryRequestDTO(
                "Category with empty description", null, null
        );

        CargoCategory category = CargoCategory.builder()
                .id(1L)
                .name("Category with empty description")
                .parentCategoryId(null)
                .description(null)
                .build();

        CargoCategoryResponseDTO response = new CargoCategoryResponseDTO(
                1L, "Category with empty description", null, null, null, List.of(), 0
        );

        when(cargoCategoryMapper.toEntity(requestWithEmptyDescription)).thenReturn(category);
        when(cargoCategoryRepository.save(category)).thenReturn(category);
        when(cargoCategoryMapper.toResponseDTO(any(CargoCategory.class), any(), any(), anyInt()))
                .thenReturn(response);

        // when
        CargoCategoryResponseDTO result = cargoCategoryService.createCategory(requestWithEmptyDescription);

        // then
        assertNotNull(result);
        assertNull(result.description());
        verify(cargoCategoryRepository, times(1)).save(category);
    }
}
