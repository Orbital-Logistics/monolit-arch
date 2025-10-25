package org.orbitalLogistic.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.orbitalLogistic.dto.request.CargoCategoryRequestDTO;
import org.orbitalLogistic.dto.response.CargoCategoryResponseDTO;
import org.orbitalLogistic.entities.CargoCategory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CargoCategoryMapperTest {

    private CargoCategoryMapper cargoCategoryMapper;

    @BeforeEach
    void setUp() {
        cargoCategoryMapper = new CargoCategoryMapperImpl();
    }

    @Test
    void toResponseDTO_WithAllFields_ShouldMapCorrectly() {
        CargoCategory cargoCategory = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices and components")
                .parentCategoryId(10L)
                .build();

        String parentCategoryName = "Technology";
        List<CargoCategoryResponseDTO> children = List.of(
                new CargoCategoryResponseDTO(2L, "Smartphones", 1L, "Electronics", "Mobile phones", null, 2),
                new CargoCategoryResponseDTO(3L, "Laptops", 1L, "Electronics", "Portable computers", null, 2)
        );
        Integer level = 1;

        CargoCategoryResponseDTO result = cargoCategoryMapper.toResponseDTO(
                cargoCategory, parentCategoryName, children, level
        );

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Electronics", result.name());
        assertEquals("Electronic devices and components", result.description());
        assertEquals("Technology", result.parentCategoryName());
        assertEquals(2, result.children().size());
        assertEquals(1, result.level());
    }

    @Test
    void toResponseDTO_WithNullParentCategory_ShouldMapCorrectly() {
        CargoCategory cargoCategory = CargoCategory.builder()
                .id(1L)
                .name("Root Category")
                .description("Root category description")
                .parentCategoryId(null)
                .build();

        CargoCategoryResponseDTO result = cargoCategoryMapper.toResponseDTO(
                cargoCategory, null, null, 0
        );

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Root Category", result.name());
        assertEquals("Root category description", result.description());
        assertNull(result.parentCategoryName());
        assertNull(result.children());
        assertEquals(0, result.level());
    }

    @Test
    void toResponseDTO_WithEmptyChildren_ShouldMapCorrectly() {
        CargoCategory cargoCategory = CargoCategory.builder()
                .id(1L)
                .name("Category")
                .description("Category description")
                .parentCategoryId(5L)
                .build();

        CargoCategoryResponseDTO result = cargoCategoryMapper.toResponseDTO(
                cargoCategory, "Parent Category", List.of(), 1
        );

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Category", result.name());
        assertEquals("Parent Category", result.parentCategoryName());
        assertNotNull(result.children());
        assertTrue(result.children().isEmpty());
        assertEquals(1, result.level());
    }

    @Test
    void toEntity_WithValidRequest_ShouldMapCorrectly() {
        CargoCategoryRequestDTO request = new CargoCategoryRequestDTO(
                "Electronics",
                10L,
                "Electronic devices and components"
        );

        CargoCategory result = cargoCategoryMapper.toEntity(request);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Electronics", result.getName());
        assertEquals("Electronic devices and components", result.getDescription());
        assertEquals(10L, result.getParentCategoryId());
    }

    @Test
    void toEntity_WithNullParentCategoryId_ShouldMapCorrectly() {
        CargoCategoryRequestDTO request = new CargoCategoryRequestDTO(
                "Root Category",
                null,
                "Root category description"
        );

        CargoCategory result = cargoCategoryMapper.toEntity(request);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Root Category", result.getName());
        assertEquals("Root category description", result.getDescription());
        assertNull(result.getParentCategoryId());
    }

    @Test
    void toEntity_WithMinimalData_ShouldMapCorrectly() {
        CargoCategoryRequestDTO request = new CargoCategoryRequestDTO(
                "Category",
                null,
                null
        );

        CargoCategory result = cargoCategoryMapper.toEntity(request);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Category", result.getName());
        assertNull(result.getDescription());
        assertNull(result.getParentCategoryId());
    }

    @Test
    void toResponseDTOList_WithMultipleCategories_ShouldMapAll() {
        CargoCategory category1 = CargoCategory.builder()
                .id(1L)
                .name("Category 1")
                .description("Description 1")
                .parentCategoryId(null)
                .build();

        CargoCategory category2 = CargoCategory.builder()
                .id(2L)
                .name("Category 2")
                .description("Description 2")
                .parentCategoryId(1L)
                .build();

        List<CargoCategory> categories = Arrays.asList(category1, category2);

        List<CargoCategoryResponseDTO> result = cargoCategoryMapper.toResponseDTOList(categories);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).id());
        assertEquals("Category 1", result.get(0).name());
        assertEquals("Description 1", result.get(0).description());

        assertEquals(2L, result.get(1).id());
        assertEquals("Category 2", result.get(1).name());
        assertEquals("Description 2", result.get(1).description());
    }

    @Test
    void toResponseDTOList_WithEmptyList_ShouldReturnEmptyList() {
        List<CargoCategory> categories = List.of();

        List<CargoCategoryResponseDTO> result = cargoCategoryMapper.toResponseDTOList(categories);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toResponseDTOList_WithNullList_ShouldReturnNull() {
        List<CargoCategoryResponseDTO> result = cargoCategoryMapper.toResponseDTOList(null);

        assertNull(result);
    }

    @Test
    void toEntity_WithNullRequest_ShouldReturnNull() {
        CargoCategory result = cargoCategoryMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void toResponseDTO_WithPartialNullParameters_ShouldMapCorrectly() {
        CargoCategory cargoCategory = CargoCategory.builder()
                .id(1L)
                .name("Category")
                .description("Description")
                .parentCategoryId(10L)
                .build();

        CargoCategoryResponseDTO result = cargoCategoryMapper.toResponseDTO(
                cargoCategory, null, null, null
        );

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Category", result.name());
        assertEquals("Description", result.description());
        assertNull(result.parentCategoryName());
        assertNull(result.children());
        assertNull(result.level());
    }

    @Test
    void toResponseDTO_WithComplexChildrenStructure_ShouldMapCorrectly() {
        CargoCategory parentCategory = CargoCategory.builder()
                .id(1L)
                .name("Parent")
                .description("Parent category")
                .parentCategoryId(null)
                .build();

        CargoCategoryResponseDTO grandChild1 = new CargoCategoryResponseDTO(
                3L, "Grandchild 1", 2L, "Child 1", "Grandchild description", null, 2
        );
        CargoCategoryResponseDTO grandChild2 = new CargoCategoryResponseDTO(
                4L, "Grandchild 2", 2L, "Child 1", "Grandchild description", null, 2
        );

        CargoCategoryResponseDTO child1 = new CargoCategoryResponseDTO(
                2L, "Child 1", 1L, "Parent", "Child description",
                List.of(grandChild1, grandChild2), 1
        );

        List<CargoCategoryResponseDTO> children = List.of(child1);

        CargoCategoryResponseDTO result = cargoCategoryMapper.toResponseDTO(
                parentCategory, null, children, 0
        );

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Parent", result.name());
        assertNotNull(result.children());
        assertEquals(1, result.children().size());

        CargoCategoryResponseDTO firstChild = result.children().get(0);
        assertEquals("Child 1", firstChild.name());
        assertNotNull(firstChild.children());
        assertEquals(2, firstChild.children().size());
    }

    @Test
    void toResponseDTO_WithMaxFieldLengths_ShouldMapCorrectly() {
        String longName = "A".repeat(100);
        String longDescription = "B".repeat(500);

        CargoCategory cargoCategory = CargoCategory.builder()
                .id(Long.MAX_VALUE)
                .name(longName)
                .description(longDescription)
                .parentCategoryId(Long.MAX_VALUE)
                .build();

        String longParentName = "C".repeat(100);
        List<CargoCategoryResponseDTO> children = List.of();
        Integer maxLevel = Integer.MAX_VALUE;

        CargoCategoryResponseDTO result = cargoCategoryMapper.toResponseDTO(
                cargoCategory, longParentName, children, maxLevel
        );

        assertNotNull(result);
        assertEquals(Long.MAX_VALUE, result.id());
        assertEquals(longName, result.name());
        assertEquals(longDescription, result.description());
        assertEquals(longParentName, result.parentCategoryName());
        assertNotNull(result.children());
        assertTrue(result.children().isEmpty());
        assertEquals(Integer.MAX_VALUE, result.level());
    }
}
