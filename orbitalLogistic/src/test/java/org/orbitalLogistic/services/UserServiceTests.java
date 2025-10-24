package org.orbitalLogistic.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private UserRole testRole;
    private UserResponseDTO testResponseDTO;
    private UserRegistrationRequestDTO testRegistrationRequest;
    private UpdateUserRequestDTO testUpdateRequest;

    @BeforeEach
    void setUp() {
        testRole = UserRole.builder()
                .id(1L)
                .name("logistics_officer")
                .build();

        testUser = User.builder()
                .id(1L)
                .email("john.doe@example.com")
                .username("John Doe")
                .passwordHash("hashedPassword")
                .roleId(1L)
                .build();

        testResponseDTO = new UserResponseDTO(
                1L, "john.doe@example.com", "John Doe"
        );

        testRegistrationRequest = new UserRegistrationRequestDTO(
                "jane.doe@example.com", "Jane Doe", "password123"
        );

        testUpdateRequest = new UpdateUserRequestDTO("Jane Doe Updated");
    }


    @Test
    void registerUser_WithValidRequest_ShouldCreateUser() {

        User newUser = User.builder()
                .email("jane.doe@example.com")
                .username("Jane Doe")
                .passwordHash("encodedPassword")
                .roleId(1L)
                .build();

        User savedUser = User.builder()
                .id(2L)
                .email("jane.doe@example.com")
                .username("Jane Doe")
                .passwordHash("encodedPassword")
                .roleId(1L)
                .build();

        UserResponseDTO expectedResponse = new UserResponseDTO(
                2L, "jane.doe@example.com", "Jane Doe"
        );

        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(userMapper.toEntity(testRegistrationRequest)).thenReturn(newUser);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("logistics_officer")).thenReturn(Optional.of(testRole));
        when(userRepository.save(newUser)).thenReturn(savedUser);
        when(userMapper.toResponseDTO(savedUser)).thenReturn(expectedResponse);


        UserResponseDTO result = userService.registerUser(testRegistrationRequest);


        assertNotNull(result);
        assertEquals("jane.doe@example.com", result.email());
        assertEquals("Jane Doe", result.username());
        verify(userRepository, times(1)).existsByEmail("jane.doe@example.com");
        verify(userMapper, times(1)).toEntity(testRegistrationRequest);
        verify(passwordEncoder, times(1)).encode("password123");
        verify(roleRepository, times(1)).findByName("logistics_officer");
        verify(userRepository, times(1)).save(newUser);
        verify(userMapper, times(1)).toResponseDTO(savedUser);
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {

        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);


        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(testRegistrationRequest)
        );

        assertEquals("User with email already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("jane.doe@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException2() {

        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);


        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(testRegistrationRequest)
        );

        assertEquals("User with email already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("jane.doe@example.com");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void getUsers_WithValidFilters_ShouldReturnPageResponse() {

        List<User> users = List.of(testUser);
        when(userRepository.findUsersWithFilters("john.doe@example.com", "John Doe", 10, 0))
                .thenReturn(users);
        when(userRepository.countUsersWithFilters("john.doe@example.com", "John Doe"))
                .thenReturn(1L);
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponseDTO);


        PageResponseDTO<UserResponseDTO> result = userService.getUsers("john.doe@example.com", "John Doe", 0, 10);


        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.currentPage());
        assertEquals(10, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.first());
        assertTrue(result.last());
        verify(userRepository, times(1)).findUsersWithFilters("john.doe@example.com", "John Doe", 10, 0);
        verify(userRepository, times(1)).countUsersWithFilters("john.doe@example.com", "John Doe");
    }

    @Test
    void getUsers_WithNullFilters_ShouldReturnAllUsers() {

        List<User> users = List.of(testUser);
        when(userRepository.findUsersWithFilters(null, null, 20, 0))
                .thenReturn(users);
        when(userRepository.countUsersWithFilters(null, null))
                .thenReturn(1L);
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponseDTO);


        PageResponseDTO<UserResponseDTO> result = userService.getUsers(null, null, 0, 20);


        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(userRepository, times(1)).findUsersWithFilters(null, null, 20, 0);
    }

    @Test
    void findUserById_WithValidId_ShouldReturnUser() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponseDTO);


        UserResponseDTO result = userService.findUserById(1L);


        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("john.doe@example.com", result.email());
        assertEquals("John Doe", result.username());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void findUserById_WithInvalidId_ShouldThrowException() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());


        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.findUserById(999L)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void findUserByEmail_WithValidEmail_ShouldReturnUser() {

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponseDTO);


        UserResponseDTO result = userService.findUserByEmail("john.doe@example.com");


        assertNotNull(result);
        assertEquals("john.doe@example.com", result.email());
        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
    }

    @Test
    void findUserByEmail_WithInvalidEmail_ShouldThrowException() {

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());


        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.findUserByEmail("nonexistent@example.com")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void updateUser_WithValidId_ShouldUpdateUser() {

        User updatedUser = User.builder()
                .id(1L)
                .email("john.doe@example.com")
                .username("Jane Doe Updated")
                .passwordHash("hashedPassword")
                .roleId(1L)
                .build();

        UserResponseDTO updatedResponse = new UserResponseDTO(
                1L, "john.doe@example.com", "Jane Doe Updated"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponseDTO(updatedUser)).thenReturn(updatedResponse);


        UserResponseDTO result = userService.updateUser(1L, testUpdateRequest);


        assertNotNull(result);
        assertEquals("Jane Doe Updated", result.username());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_WithInvalidId_ShouldThrowException() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());


        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(999L, testUpdateRequest)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WithNullUsername_ShouldNotUpdateUsername() {

        UpdateUserRequestDTO requestWithNullUsername = new UpdateUserRequestDTO(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponseDTO);


        UserResponseDTO result = userService.updateUser(1L, requestWithNullUsername);


        assertNotNull(result);
        assertEquals("John Doe", result.username());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {

        when(userRepository.existsById(1L)).thenReturn(true);


        userService.deleteUser(1L);


        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_WithInvalidId_ShouldThrowException() {

        when(userRepository.existsById(999L)).thenReturn(false);


        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(999L)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void toResponseDTO_WithValidUser_ShouldReturnResponseDTO() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponseDTO);


        UserResponseDTO result = userService.findUserById(1L);


        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("john.doe@example.com", result.email());
        assertEquals("John Doe", result.username());
        verify(userMapper, times(1)).toResponseDTO(testUser);
    }

    @Test
    void getUsers_WithEmptyResult_ShouldReturnEmptyPage() {

        when(userRepository.findUsersWithFilters(null, null, 20, 0)).thenReturn(List.of());
        when(userRepository.countUsersWithFilters(null, null)).thenReturn(0L);


        PageResponseDTO<UserResponseDTO> result = userService.getUsers(null, null, 0, 20);


        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        assertEquals(0, result.totalPages());
        assertTrue(result.first());
        assertTrue(result.last());
    }

    @Test
    void getUsers_WithMultiplePages_ShouldCalculatePaginationCorrectly() {

        List<User> users = List.of(testUser);
        when(userRepository.findUsersWithFilters(null, null, 10, 10)).thenReturn(users);
        when(userRepository.countUsersWithFilters(null, null)).thenReturn(25L);
        when(userMapper.toResponseDTO(testUser)).thenReturn(testResponseDTO);


        PageResponseDTO<UserResponseDTO> result = userService.getUsers(null, null, 1, 10);


        assertNotNull(result);
        assertEquals(1, result.currentPage());
        assertEquals(10, result.pageSize());
        assertEquals(25, result.totalElements());
        assertEquals(3, result.totalPages());
        assertFalse(result.first());
        assertFalse(result.last());
    }

    @Test
    void registerUser_WithMissingRole_ShouldThrowException() {

        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(userMapper.toEntity(testRegistrationRequest)).thenReturn(testUser);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("logistics_officer")).thenReturn(Optional.empty());


        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> userService.registerUser(testRegistrationRequest)
        );

        assertEquals("logistics_officer role not found", exception.getMessage());
        verify(roleRepository, times(1)).findByName("logistics_officer");
        verify(userRepository, never()).save(any());
    }



    @Test
    void registerUser_ShouldSetDefaultRoleAndEncodePassword() {

        User newUser = User.builder()
                .email("new.user@example.com")
                .username("New User")
                .passwordHash("plainPassword")
                .build();

        User savedUser = User.builder()
                .id(3L)
                .email("new.user@example.com")
                .username("New User")
                .passwordHash("encodedPassword")
                .roleId(1L)
                .build();

        UserResponseDTO expectedResponse = new UserResponseDTO(
                3L, "new.user@example.com", "New User"
        );

        when(userRepository.existsByEmail("new.user@example.com")).thenReturn(false);
        when(userMapper.toEntity(any())).thenReturn(newUser);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("logistics_officer")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponseDTO(savedUser)).thenReturn(expectedResponse);

        UserRegistrationRequestDTO newRequest = new UserRegistrationRequestDTO(
                "new.user@example.com", "New User", "password123"
        );


        UserResponseDTO result = userService.registerUser(newRequest);


        assertNotNull(result);
        assertEquals("new.user@example.com", result.email());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }
}