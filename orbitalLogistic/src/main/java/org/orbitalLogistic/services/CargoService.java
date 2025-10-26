package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.CargoRequestDTO;
import org.orbitalLogistic.dto.response.CargoResponseDTO;
import org.orbitalLogistic.entities.Cargo;
import org.orbitalLogistic.entities.CargoCategory;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.exceptions.CargoAlreadyExistsException;
import org.orbitalLogistic.exceptions.CargoNotFoundException;
import org.orbitalLogistic.exceptions.InvalidOperationException;
import org.orbitalLogistic.mappers.CargoMapper;
import org.orbitalLogistic.repositories.CargoRepository;
import org.orbitalLogistic.repositories.CargoCategoryRepository;
import org.orbitalLogistic.repositories.CargoManifestRepository;
import org.orbitalLogistic.repositories.CargoStorageRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CargoService {

    private final CargoRepository cargoRepository;
    private final CargoCategoryRepository cargoCategoryRepository;
    private final CargoManifestRepository cargoManifestRepository;
    private final CargoStorageRepository cargoStorageRepository;
    private final CargoMapper cargoMapper;

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

        CargoCategory cargoCategory = cargoCategoryRepository.findById(request.cargoCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Cargo category not found"));

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

        CargoCategory cargoCategory = cargoCategoryRepository.findById(request.cargoCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Cargo category not found"));

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

        boolean hasManifests = cargoManifestRepository.existsByCargoId(id);
        if (hasManifests) {
            throw new InvalidOperationException("Cannot delete cargo: it is used in cargo manifests");
        }

        cargoRepository.deleteById(id);
    }

    public PageResponseDTO<CargoResponseDTO> searchCargos(String name, String cargoType, String hazardLevel, int page, int size) {
        return getCargosPaged(name, cargoType, hazardLevel, page, size);
    }

    private CargoResponseDTO toResponseDTO(Cargo cargo) {
        CargoCategory cargoCategory = cargoCategoryRepository.findById(cargo.getCargoCategoryId())
                .orElseThrow(() -> new DataNotFoundException("Cargo category not found"));

        Integer totalQuantity = calculateTotalQuantity(cargo);

        return cargoMapper.toResponseDTO(
            cargo,
            cargoCategory.getName(),
            totalQuantity
        );
    }

    private Integer calculateTotalQuantity(Cargo cargo) {
        return cargoStorageRepository.findByCargoId(cargo.getId())
                .stream()
                .mapToInt(storage -> storage.getQuantity() != null ? storage.getQuantity() : 0)
                .sum();
    }
}
