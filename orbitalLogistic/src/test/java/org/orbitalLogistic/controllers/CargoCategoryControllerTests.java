package org.orbitalLogistic.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.request.CargoCategoryRequestDTO;
import org.orbitalLogistic.dto.response.CargoCategoryResponseDTO;
import org.orbitalLogistic.services.CargoCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoCategoryControllerTests {

    @Mock
    private CargoCategoryService cargoCategoryService;

    @InjectMocks
    private CargoCategoryController cargoCategoryController;

    private CargoCategoryResponseDTO testCategoryResponse;
    private CargoCategoryRequestDTO testCategoryRequest;

    @BeforeEach
    void setUp() {
        testCategoryResponse = new CargoCategoryResponseDTO(
                1L,
                "Scientific Equipment",
                null,
                null,
                "Category for scientific tools",
                List.of(),
                0
        );

        testCategoryRequest = new CargoCategoryRequestDTO(
                "Scientific Equipment",
                null,
                "Category for scientific tools"
        );
    }

    @Test
    void getAllCategories_ShouldReturnListOfCategories() {

        when(cargoCategoryService.getAllCategories()).thenReturn(List.of(testCategoryResponse));


        ResponseEntity<List<CargoCategoryResponseDTO>> response = cargoCategoryController.getAllCategories();


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Scientific Equipment", response.getBody().get(0).name());
        verify(cargoCategoryService, times(1)).getAllCategories();
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {

        when(cargoCategoryService.getCategoryById(1L)).thenReturn(testCategoryResponse);


        ResponseEntity<CargoCategoryResponseDTO> response = cargoCategoryController.getCategoryById(1L);


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Scientific Equipment", response.getBody().name());
        verify(cargoCategoryService, times(1)).getCategoryById(1L);
    }

    @Test
    void getCategoryById_WithInvalidId_ShouldPropagateException() {

        when(cargoCategoryService.getCategoryById(999L))
                .thenThrow(new IllegalArgumentException("Category not found with id: 999"));


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cargoCategoryController.getCategoryById(999L)
        );

        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(cargoCategoryService, times(1)).getCategoryById(999L);
    }

    @Test
    void createCategory_ShouldReturnCreatedResponse() {

        when(cargoCategoryService.createCategory(testCategoryRequest)).thenReturn(testCategoryResponse);


        ResponseEntity<CargoCategoryResponseDTO> response = cargoCategoryController.createCategory(testCategoryRequest);


        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Scientific Equipment", response.getBody().name());
        verify(cargoCategoryService, times(1)).createCategory(testCategoryRequest);
    }

    @Test
    void createCategory_WithExistingName_ShouldPropagateException() {

        when(cargoCategoryService.createCategory(testCategoryRequest))
                .thenThrow(new IllegalArgumentException("Category with name already exists: Scientific Equipment"));


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cargoCategoryController.createCategory(testCategoryRequest)
        );

        assertEquals("Category with name already exists: Scientific Equipment", exception.getMessage());
        verify(cargoCategoryService, times(1)).createCategory(testCategoryRequest);
    }

    @Test
    void getCategoryTree_ShouldReturnNestedCategories() {

        CargoCategoryResponseDTO childCategory = new CargoCategoryResponseDTO(
                2L,
                "Subcategory A",
                1L,
                "Scientific Equipment",
                "Child category",
                List.of(),
                1
        );

        CargoCategoryResponseDTO parentCategory = new CargoCategoryResponseDTO(
                1L,
                "Scientific Equipment",
                null,
                null,
                "Parent category",
                List.of(childCategory),
                0
        );

        when(cargoCategoryService.getCategoryTree()).thenReturn(List.of(parentCategory));


        ResponseEntity<List<CargoCategoryResponseDTO>> response = cargoCategoryController.getCategoryTree();


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Scientific Equipment", response.getBody().get(0).name());
        assertEquals(1, response.getBody().get(0).children().size());
        assertEquals("Subcategory A", response.getBody().get(0).children().get(0).name());
        verify(cargoCategoryService, times(1)).getCategoryTree();
    }

    @Test
    void getCategoryTree_WithNoCategories_ShouldReturnEmptyList() {

        when(cargoCategoryService.getCategoryTree()).thenReturn(List.of());


        ResponseEntity<List<CargoCategoryResponseDTO>> response = cargoCategoryController.getCategoryTree();


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(cargoCategoryService, times(1)).getCategoryTree();
    }
}
