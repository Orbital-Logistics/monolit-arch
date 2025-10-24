package org.orbitalLogistic.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.dto.request.UpdateUserRequestDTO;
import org.orbitalLogistic.dto.request.UserRegistrationRequestDTO;
import org.orbitalLogistic.dto.response.UserResponseDTO;
import org.orbitalLogistic.exceptions.user.UserAlreadyExistsException;
import org.orbitalLogistic.exceptions.user.UserNotFoundException;
import org.orbitalLogistic.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserResponseDTO testUserResponse;
    private UserRegistrationRequestDTO testRegistrationRequest;
    private UpdateUserRequestDTO testUpdateRequest;

    @BeforeEach
    void setUp() {
        testUserResponse = new UserResponseDTO(
                1L,
                "test@example.com",
                "testuser"
        );

        testRegistrationRequest = new UserRegistrationRequestDTO(
                "test@example.com",
                "testuser",
                "password123"
        );

        testUpdateRequest = new UpdateUserRequestDTO(
                "updateduser"
        );
    }

    @Test
    void registerUser_WithValidRequest_ShouldReturnCreatedResponse() {
        when(userService.registerUser(testRegistrationRequest))
                .thenReturn(testUserResponse);

        ResponseEntity<UserResponseDTO> response =
                userController.registerUser(testRegistrationRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("test@example.com", response.getBody().email());
        assertEquals("testuser", response.getBody().username());
        verify(userService, times(1)).registerUser(testRegistrationRequest);
    }

    @Test
    void registerUser_WithExistingEmail_ShouldPropagateException() {
        when(userService.registerUser(testRegistrationRequest))
                .thenThrow(new UserAlreadyExistsException("User with email already exists"));

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userController.registerUser(testRegistrationRequest)
        );

        assertEquals("User with email already exists", exception.getMessage());
        verify(userService, times(1)).registerUser(testRegistrationRequest);
    }

    @Test
    void registerUser_WithNullRequest_ShouldPropagateException() {
        when(userService.registerUser(null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.registerUser(null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(userService, times(1)).registerUser(null);
    }

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUser() {
        when(userService.findUserByEmail("test@example.com"))
                .thenReturn(testUserResponse);

        ResponseEntity<UserResponseDTO> response =
                userController.getUserByEmail("test@example.com");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().email());
        assertEquals("testuser", response.getBody().username());
        verify(userService, times(1)).findUserByEmail("test@example.com");
    }

    @Test
    void getUserByEmail_WithInvalidEmail_ShouldPropagateException() {
        when(userService.findUserByEmail("nonexistent@example.com"))
                .thenThrow(new UserNotFoundException("User not found"));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.getUserByEmail("nonexistent@example.com")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).findUserByEmail("nonexistent@example.com");
    }

    @Test
    void getUserByEmail_WithEmptyEmail_ShouldPropagateException() {
        when(userService.findUserByEmail(""))
                .thenThrow(new UserNotFoundException("User not found"));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.getUserByEmail("")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).findUserByEmail("");
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        when(userService.findUserById(1L))
                .thenReturn(testUserResponse);

        ResponseEntity<UserResponseDTO> response =
                userController.getUserById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("test@example.com", response.getBody().email());
        verify(userService, times(1)).findUserById(1L);
    }

    @Test
    void getUserById_WithInvalidId_ShouldPropagateException() {
        when(userService.findUserById(999L))
                .thenThrow(new UserNotFoundException("User not found"));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.getUserById(999L)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).findUserById(999L);
    }

    @Test
    void getUserById_WithNullId_ShouldPropagateException() {
        when(userService.findUserById(null))
                .thenThrow(new IllegalArgumentException("ID cannot be null"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.getUserById(null)
        );

        assertEquals("ID cannot be null", exception.getMessage());
        verify(userService, times(1)).findUserById(null);
    }

    @Test
    void updateUser_WithValidRequest_ShouldReturnUpdatedUser() {
        UserResponseDTO updatedUser = new UserResponseDTO(
                1L,
                "test@example.com",
                "updateduser"
        );

        when(userService.updateUser(1L, testUpdateRequest))
                .thenReturn(updatedUser);

        ResponseEntity<UserResponseDTO> response =
                userController.updateUser(1L, testUpdateRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("updateduser", response.getBody().username());
        assertEquals("test@example.com", response.getBody().email());
        verify(userService, times(1)).updateUser(1L, testUpdateRequest);
    }

    @Test
    void updateUser_WithInvalidId_ShouldPropagateException() {
        when(userService.updateUser(999L, testUpdateRequest))
                .thenThrow(new UserNotFoundException("User not found"));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.updateUser(999L, testUpdateRequest)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).updateUser(999L, testUpdateRequest);
    }

    @Test
    void updateUser_WithNullRequest_ShouldPropagateException() {
        when(userService.updateUser(1L, null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.updateUser(1L, null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(userService, times(1)).updateUser(1L, null);
    }

    @Test
    void updateUser_WithNullId_ShouldPropagateException() {
        when(userService.updateUser(null, testUpdateRequest))
                .thenThrow(new IllegalArgumentException("ID cannot be null"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.updateUser(null, testUpdateRequest)
        );

        assertEquals("ID cannot be null", exception.getMessage());
        verify(userService, times(1)).updateUser(null, testUpdateRequest);
    }

    @Test
    void deleteUser_WithValidId_ShouldReturnNoContent() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_WithInvalidId_ShouldPropagateException() {
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).deleteUser(999L);

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.deleteUser(999L)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).deleteUser(999L);
    }

    @Test
    void deleteUser_WithNullId_ShouldPropagateException() {
        doThrow(new IllegalArgumentException("ID cannot be null"))
                .when(userService).deleteUser(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.deleteUser(null)
        );

        assertEquals("ID cannot be null", exception.getMessage());
        verify(userService, times(1)).deleteUser(null);
    }

    @Test
    void registerUser_WithServiceException_ShouldPropagateException() {
        when(userService.registerUser(testRegistrationRequest))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userController.registerUser(testRegistrationRequest)
        );

        assertEquals("Database error", exception.getMessage());
        verify(userService, times(1)).registerUser(testRegistrationRequest);
    }

    @Test
    void getUserByEmail_WithServiceException_ShouldPropagateException() {
        when(userService.findUserByEmail("test@example.com"))
                .thenThrow(new RuntimeException("Service error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userController.getUserByEmail("test@example.com")
        );

        assertEquals("Service error", exception.getMessage());
        verify(userService, times(1)).findUserByEmail("test@example.com");
    }

    @Test
    void getUserById_WithServiceException_ShouldPropagateException() {
        when(userService.findUserById(1L))
                .thenThrow(new RuntimeException("Service error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userController.getUserById(1L)
        );

        assertEquals("Service error", exception.getMessage());
        verify(userService, times(1)).findUserById(1L);
    }

    @Test
    void updateUser_WithServiceException_ShouldPropagateException() {
        when(userService.updateUser(1L, testUpdateRequest))
                .thenThrow(new RuntimeException("Update error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userController.updateUser(1L, testUpdateRequest)
        );

        assertEquals("Update error", exception.getMessage());
        verify(userService, times(1)).updateUser(1L, testUpdateRequest);
    }

    @Test
    void deleteUser_WithServiceException_ShouldPropagateException() {
        doThrow(new RuntimeException("Delete error"))
                .when(userService).deleteUser(1L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userController.deleteUser(1L)
        );

        assertEquals("Delete error", exception.getMessage());
        verify(userService, times(1)).deleteUser(1L);
    }
}
