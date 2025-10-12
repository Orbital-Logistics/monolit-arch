package org.orbitalLogistic.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.exceptions.user.UserAlreadyExistsException;
import org.orbitalLogistic.exceptions.user.UserNotFoundException;
import org.orbitalLogistic.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

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
    private PageResponseDTO<UserResponseDTO> testPageResponse;

    @BeforeEach
    void setUp() {
        testUserResponse = new UserResponseDTO(
                1L, "john.doe@example.com", "John", "USER"
        );

        testRegistrationRequest = new UserRegistrationRequestDTO(
                "jane.doe@example.com", "Jane", "password123"
        );

        testUpdateRequest = new UpdateUserRequestDTO("Jane Updated");

        testPageResponse = new PageResponseDTO<>(
                List.of(testUserResponse), 0, 20, 1L, 1, true, true
        );
    }

    @Test
    void registerUser_WithValidRequest_ShouldReturnCreatedResponse() {
        // given
        when(userService.registerUser(testRegistrationRequest)).thenReturn(testUserResponse);

        // when
        ResponseEntity<UserResponseDTO> response = userController.registerUser(testRegistrationRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe@example.com", response.getBody().email());
        verify(userService, times(1)).registerUser(testRegistrationRequest);
    }

    @Test
    void registerUser_WithExistingEmail_ShouldPropagateException() {
        // given
        when(userService.registerUser(testRegistrationRequest))
                .thenThrow(new UserAlreadyExistsException("User with email already exists"));

        // when & then
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userController.registerUser(testRegistrationRequest)
        );

        assertEquals("User with email already exists", exception.getMessage());
        verify(userService, times(1)).registerUser(testRegistrationRequest);
    }

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUser() {
        // given
        when(userService.findUserByEmail("john.doe@example.com")).thenReturn(testUserResponse);

        // when
        ResponseEntity<UserResponseDTO> response = userController.getUserByEmail("john.doe@example.com");

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe@example.com", response.getBody().email());
        verify(userService, times(1)).findUserByEmail("john.doe@example.com");
    }

    @Test
    void getUserByEmail_WithInvalidEmail_ShouldPropagateException() {
        // given
        when(userService.findUserByEmail("nonexistent@example.com"))
                .thenThrow(new UserNotFoundException("User not found"));

        // when & then
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.getUserByEmail("nonexistent@example.com")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).findUserByEmail("nonexistent@example.com");
    }

    @Test
    void getUsers_WithValidParameters_ShouldReturnPageResponse() {
        // given
        when(userService.getUsers("john.doe@example.com", "John", 0, 20))
                .thenReturn(testPageResponse);

        // when
        ResponseEntity<PageResponseDTO<UserResponseDTO>> response = userController.getUsers(
                "john.doe@example.com", "John", 0, 20
        );

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().content().size());
        verify(userService, times(1)).getUsers("john.doe@example.com", "John", 0, 20);
    }

    @Test
    void getUsers_WithNullParameters_ShouldReturnAllUsers() {
        // given
        when(userService.getUsers(null, null, 0, 20)).thenReturn(testPageResponse);

        // when
        ResponseEntity<PageResponseDTO<UserResponseDTO>> response = userController.getUsers(
                null, null, 0, 20
        );

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).getUsers(null, null, 0, 20);
    }

    @Test
    void getUsers_WithDefaultParameters_ShouldUseDefaults() {
        // given
        when(userService.getUsers(null, null, 0, 20)).thenReturn(testPageResponse);

        // when
        ResponseEntity<PageResponseDTO<UserResponseDTO>> response = userController.getUsers(
                null, null, 0, 20
        );

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).getUsers(null, null, 0, 20);
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        // given
        when(userService.findUserById(1L)).thenReturn(testUserResponse);

        // when
        ResponseEntity<UserResponseDTO> response = userController.getUserById(1L);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        verify(userService, times(1)).findUserById(1L);
    }

    @Test
    void getUserById_WithInvalidId_ShouldPropagateException() {
        // given
        when(userService.findUserById(999L))
                .thenThrow(new UserNotFoundException("User not found"));

        // when & then
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.getUserById(999L)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).findUserById(999L);
    }

    @Test
    void updateUser_WithInvalidId_ShouldPropagateException() {
        // given
        when(userService.updateUser(999L, testUpdateRequest))
                .thenThrow(new UserNotFoundException("User not found"));

        // when & then
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.updateUser(999L, testUpdateRequest)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).updateUser(999L, testUpdateRequest);
    }

    @Test
    void deleteUser_WithValidId_ShouldReturnNoContent() {
        // given
        doNothing().when(userService).deleteUser(1L);

        // when
        ResponseEntity<Void> response = userController.deleteUser(1L);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_WithInvalidId_ShouldPropagateException() {
        // given
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).deleteUser(999L);

        // when & then
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.deleteUser(999L)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(1)).deleteUser(999L);
    }

    @Test
    void registerUser_WithNullRequest_ShouldPropagateException() {
        // given
        when(userService.registerUser(null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.registerUser(null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(userService, times(1)).registerUser(null);
    }

    @Test
    void updateUser_WithNullRequest_ShouldPropagateException() {
        // given
        when(userService.updateUser(1L, null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userController.updateUser(1L, null)
        );

        assertEquals("Request cannot be null", exception.getMessage());
        verify(userService, times(1)).updateUser(1L, null);
    }
}
