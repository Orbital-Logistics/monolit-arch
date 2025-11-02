package org.orbitalLogistic.services;

import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoRequestDTO;
import org.orbitalLogistic.dto.response.CargoResponseDTO;
import org.orbitalLogistic.entities.Cargo;
import org.orbitalLogistic.entities.CargoCategory;
import org.orbitalLogistic.exceptions.CargoAlreadyExistsException;
import org.orbitalLogistic.exceptions.CargoNotFoundException;
import org.orbitalLogistic.exceptions.InvalidOperationException;
import org.orbitalLogistic.mappers.CargoMapper;
import org.orbitalLogistic.repositories.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CargoService {

    private final CargoRepository cargoRepository;
    private final CargoMapper cargoMapper;

    private CargoCategoryService cargoCategoryService;
    private CargoManifestService cargoManifestService;
    private CargoStorageService cargoStorageService;

    public CargoService(CargoRepository cargoRepository, CargoMapper cargoMapper) {
        this.cargoRepository = cargoRepository;
        this.cargoMapper = cargoMapper;
    }

    @Autowired
    public void setCargoCategoryService(@Lazy CargoCategoryService cargoCategoryService) {
        this.cargoCategoryService = cargoCategoryService;
    }

    @Autowired
    public void setCargoManifestService(@Lazy CargoManifestService cargoManifestService) {
        this.cargoManifestService = cargoManifestService;
    }

    @Autowired
    public void setCargoStorageService(@Lazy CargoStorageService cargoStorageService) {
        this.cargoStorageService = cargoStorageService;
    }

    public List<CargoResponseDTO> getCargosScroll(int page, int size) {
        int offset = page * size;
        List<Cargo> cargos = cargoRepository.findWithFilters(null, null, null, size + 1, offset);
        
        return cargos.stream()
                .limit(size)
                .map(this::toResponseDTO)
                .toList();
    }

    public PageResponseDTO<CargoResponseDTO> getCargosPaged(String name, String cargoType, String hazardLevel, int page, int size) {
        int offset = page * size;
        List<Cargo> cargos = cargoRepository.findWithFilters(name, cargoType, hazardLevel, size, offset);
        long total = cargoRepository.countWithFilters(name, cargoType, hazardLevel);

        List<CargoResponseDTO> cargoDTOs = cargos.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(cargoDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public CargoResponseDTO getCargoById(Long id) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + id));
        return toResponseDTO(cargo);
    }

    public CargoResponseDTO createCargo(CargoRequestDTO request) {
        if (cargoRepository.existsByName(request.name())) {
            throw new CargoAlreadyExistsException("Cargo with name already exists: " + request.name());
        }

        CargoCategory cargoCategory = cargoCategoryService.getEntityById(request.cargoCategoryId());

        Cargo cargo = cargoMapper.toEntity(request);
        cargo.setCargoCategoryId(cargoCategory.getId());

        Cargo saved = cargoRepository.save(cargo);
        return toResponseDTO(saved);
    }

    public CargoResponseDTO updateCargo(Long id, CargoRequestDTO request) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + id));

        if (!cargo.getName().equals(request.name()) && cargoRepository.existsByName(request.name())) {
            throw new CargoAlreadyExistsException("Cargo with name already exists: " + request.name());
        }

        CargoCategory cargoCategory = cargoCategoryService.getEntityById(request.cargoCategoryId());

        cargo.setName(request.name());
        cargo.setCargoCategoryId(cargoCategory.getId());
        cargo.setMassPerUnit(request.massPerUnit());
        cargo.setVolumePerUnit(request.volumePerUnit());
        cargo.setCargoType(request.cargoType());
        cargo.setHazardLevel(request.hazardLevel());

        Cargo updated = cargoRepository.save(cargo);
        return toResponseDTO(updated);
    }

    public void deleteCargo(Long id) {
        if (!cargoRepository.existsById(id)) {
            throw new CargoNotFoundException("Cargo not found with id: " + id);
        }

        boolean hasManifests = cargoManifestService.existsByCargoId(id);
        if (hasManifests) {
            throw new InvalidOperationException("Cannot delete cargo: it is used in cargo manifests");
        }

        cargoRepository.deleteById(id);
    }

    public PageResponseDTO<CargoResponseDTO> searchCargos(String name, String cargoType, String hazardLevel, int page, int size) {
        return getCargosPaged(name, cargoType, hazardLevel, page, size);
    }

    public Cargo getEntityById(Long id) {
        return cargoRepository.findById(id)
                .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + id));
    }

    private CargoResponseDTO toResponseDTO(Cargo cargo) {
        CargoCategory cargoCategory = cargoCategoryService.getEntityById(cargo.getCargoCategoryId());

        Integer totalQuantity = cargoStorageService.calculateTotalQuantityForCargo(cargo.getId());

        return cargoMapper.toResponseDTO(
            cargo,
            cargoCategory.getName(),
            totalQuantity
        );
    }
}
