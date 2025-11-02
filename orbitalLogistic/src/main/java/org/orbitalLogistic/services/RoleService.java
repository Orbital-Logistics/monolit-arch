package org.orbitalLogistic.services;

import org.orbitalLogistic.dto.request.RoleRequestDTO;
import org.orbitalLogistic.dto.response.RoleResponseDTO;
import org.orbitalLogistic.entities.UserRole;
import org.orbitalLogistic.exceptions.RoleNotFoundException;
import org.orbitalLogistic.repositories.UserRoleRepository;
import org.springframework.stereotype.Service;


@Service
public class RoleService {

    private final UserRoleRepository userRoleRepository;

    public RoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public RoleResponseDTO createRole(RoleRequestDTO request) {
        UserRole role = UserRole.builder()
                .name(request.name())
                .build();

        UserRole savedRole = userRoleRepository.save(role);
        return toResponseDTO(savedRole);
    }

    public RoleResponseDTO getRoleById(Long id) {
        UserRole role = userRoleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        return toResponseDTO(role);
    }

    public boolean roleExists(Long id) {
        return userRoleRepository.existsById(id);
    }

    private RoleResponseDTO toResponseDTO(UserRole role) {
        return new RoleResponseDTO(
            role.getId(),
            role.getName()
        );
    }
}