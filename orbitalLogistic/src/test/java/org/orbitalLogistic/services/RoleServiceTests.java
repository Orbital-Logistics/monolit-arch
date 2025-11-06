package org.orbitalLogistic.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.request.RoleRequestDTO;
import org.orbitalLogistic.dto.response.RoleResponseDTO;
import org.orbitalLogistic.entities.UserRole;
import org.orbitalLogistic.exceptions.RoleNotFoundException;
import org.orbitalLogistic.repositories.UserRoleRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTests {

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void getRoleById_WithValidId_ShouldReturnRole() {
        UserRole role = UserRole.builder().id(5L).name("admin").build();
        when(userRoleRepository.findById(5L)).thenReturn(Optional.of(role));

        RoleResponseDTO result = roleService.getRoleById(5L);

        assertNotNull(result);
        assertEquals(5L, result.id());
        assertEquals("admin", result.name());
        verify(userRoleRepository, times(1)).findById(5L);
    }

    @Test
    void getRoleById_WithInvalidId_ShouldThrow() {
        when(userRoleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> roleService.getRoleById(999L));
        verify(userRoleRepository, times(1)).findById(999L);
    }

    @Test
    void roleExists_ShouldReturnFlags() {
        when(userRoleRepository.existsById(1L)).thenReturn(true);
        when(userRoleRepository.existsById(2L)).thenReturn(false);

        assertTrue(roleService.roleExists(1L));
        assertFalse(roleService.roleExists(2L));
        verify(userRoleRepository, times(1)).existsById(1L);
        verify(userRoleRepository, times(1)).existsById(2L);
    }
}


