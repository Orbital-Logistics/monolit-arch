package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.request.CargoCategoryRequestDTO;
import org.orbitalLogistic.dto.response.CargoCategoryResponseDTO;
import org.orbitalLogistic.entities.CargoCategory;
import org.orbitalLogistic.exceptions.CargoCategoryNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.mappers.CargoCategoryMapper;
import org.orbitalLogistic.repositories.CargoCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CargoCategoryService {

    private final CargoCategoryRepository cargoCategoryRepository;
    private final CargoCategoryMapper cargoCategoryMapper;

    public List<CargoCategoryResponseDTO> getAllCategories() {
        List<CargoCategory> categories = (List<CargoCategory>) cargoCategoryRepository.findAll();
        return categories.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public CargoCategoryResponseDTO getCategoryById(Long id) {
        CargoCategory category = cargoCategoryRepository.findById(id)
                .orElseThrow(() -> new CargoCategoryNotFoundException("Cargo category not found with id: " + id));
        return toResponseDTO(category);
    }

    // Метод требует @Transactional, так как выполняет множественные запросы к БД для построения дерева категорий с children.
    @Transactional(readOnly = true)
    public List<CargoCategoryResponseDTO> getCategoryTree() {
        List<CargoCategory> rootCategories = cargoCategoryRepository.findByParentCategoryIdIsNull();
        return rootCategories.stream()
                .map(category -> buildCategoryTree(category, 0))
                .toList();
    }

    public CargoCategoryResponseDTO createCategory(CargoCategoryRequestDTO request) {
        if (request.parentCategoryId() != null) {
            cargoCategoryRepository.findById(request.parentCategoryId())
                    .orElseThrow(() -> new DataNotFoundException("Parent category not found"));
        }

        CargoCategory category = cargoCategoryMapper.toEntity(request);
        CargoCategory saved = cargoCategoryRepository.save(category);
        return toResponseDTO(saved);
    }

    private CargoCategoryResponseDTO buildCategoryTree(CargoCategory category, int level) {
        List<CargoCategory> children = cargoCategoryRepository.findByParentCategoryId(category.getId());
        List<CargoCategoryResponseDTO> childrenDTOs = children.stream()
                .map(child -> buildCategoryTree(child, level + 1))
                .toList();

        String parentCategoryName = null;
        if (category.getParentCategoryId() != null) {
            CargoCategory parent = cargoCategoryRepository.findById(category.getParentCategoryId()).orElse(null);
            if (parent != null) {
                parentCategoryName = parent.getName();
            }
        }

        return cargoCategoryMapper.toResponseDTO(category, parentCategoryName, childrenDTOs, level);
    }

    private CargoCategoryResponseDTO toResponseDTO(CargoCategory category) {
        String parentCategoryName = null;
        if (category.getParentCategoryId() != null) {
            CargoCategory parent = cargoCategoryRepository.findById(category.getParentCategoryId()).orElse(null);
            if (parent != null) {
                parentCategoryName = parent.getName();
            }
        }

        return cargoCategoryMapper.toResponseDTO(category, parentCategoryName, new ArrayList<>(), 0);
    }
}
