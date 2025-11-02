package org.orbitalLogistic.services;

import lombok.RequiredArgsConstructor;

import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.UpdateUserRequestDTO;
import org.orbitalLogistic.dto.request.UserRegistrationRequestDTO;
import org.orbitalLogistic.dto.response.UserResponseDTO;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.entities.UserRole;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.exceptions.user.UserAlreadyExistsException;
import org.orbitalLogistic.exceptions.user.UserNotFoundException;
import org.orbitalLogistic.mappers.UserMapper;
import org.orbitalLogistic.repositories.UserRepository;
import org.orbitalLogistic.repositories.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final UserMapper userMapper;

    public UserResponseDTO registerUser(UserRegistrationRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(request.password());
        UserRole userRole = roleRepository.findByName("logistics_officer")
                .orElseThrow(() -> new DataNotFoundException("logistics_officer role not found"));
        user.setRoleId(userRole.getId());

        user = userRepository.save(user);
        return toResponseDTO(user);
    }

    public PageResponseDTO<UserResponseDTO> getUsers(String email, String username, int page, int size) {
        int offset = page * size;
        List<User> users = userRepository.findUsersWithFilters(email, username, size, offset);
        long total = userRepository.countUsersWithFilters(email, username);

        List<UserResponseDTO> userDTOs = users.stream().map(this::toResponseDTO).toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(userDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return toResponseDTO(user);
    }

    public UserResponseDTO findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponseDTO(user);
    }

    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.username() != null) user.setUsername(request.username());

        user = userRepository.save(user);
        return toResponseDTO(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public User getEntityByIdOrNull(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    private UserResponseDTO toResponseDTO(User user) {
        return userMapper.toResponseDTO(user);
    }
}
