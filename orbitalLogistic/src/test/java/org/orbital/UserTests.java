package org.orbital;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.orbitalLogistic.controllers.UserController;
import org.orbitalLogistic.dto.common.PageResponseDTO;
import org.orbitalLogistic.dto.request.UpdateUserRequestDTO;
import org.orbitalLogistic.dto.request.UserRegistrationRequestDTO;
import org.orbitalLogistic.dto.response.UserResponseDTO;
import org.orbitalLogistic.entities.User;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.exceptions.user.UserAlreadyExistsException;
import org.orbitalLogistic.exceptions.user.UserNotFoundException;
import org.orbitalLogistic.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserTests {

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    private final String testName = "user";
    private final String testEmail = "user1@example.com";
    private final String testPassword = "passHash";

    @BeforeAll
    void setUp() {
        userRepository.save(User.builder()
                        .id(1L)
                        .username(testName)
                        .email(testEmail)
                        .passwordHash(testPassword)
                        .roleId(1L)
                .build());

    }

    @Test
    void registerUserTest() {
        UserResponseDTO userResponseDTO = userController.registerUser(new UserRegistrationRequestDTO(
                "user2@example.com", testName, testPassword)).getBody();
        User user = userRepository.findById(userResponseDTO.id()).orElseThrow();
        assertEquals(userResponseDTO.id(), user.getId());
        userRepository.delete(user);
    }

    @Test
    void registerUserExistsTest() {
        UserRegistrationRequestDTO requestDTO = new UserRegistrationRequestDTO(
                testEmail, testName, testPassword);
        Exception exception = assertThrows(UserAlreadyExistsException.class,
                () -> userController.registerUser(requestDTO));

        assertEquals("User with email already exists", exception.getMessage());
    }

    @Test
    void getUsersTest() {
        PageResponseDTO<?> pageResponseDTO = userController.getUsers(testEmail, testName, 0, 50).getBody();
        assertEquals(50, pageResponseDTO.size());
    }

    @Test
    void getCurrentTest() {
        UserResponseDTO user = userController.getUserById(1L).getBody();
        assertEquals(1, user.id());

        user = userController.getUserByEmail(testEmail).getBody();
        assertEquals(testEmail, user.email());
    }

    @Test
    void getUserNotFoundTest() {
        Exception exception = assertThrows(UserNotFoundException.class, () -> userController.getUserById(-1L));
        assertEquals("User not found", exception.getMessage());

        exception = assertThrows(UserNotFoundException.class,
                () -> userController.getUserByEmail("error@example.com"));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void updateUserTest() {
        userController.updateUser(1L, new UpdateUserRequestDTO("update_user"));
        UserResponseDTO user = userController.getUserById(1L).getBody();
        assertAll(
                () -> assertEquals("update_user", user.username())
        );
    }

    @Test
    void updateUserErrorsTest() {
        Exception exception = assertThrows(UserNotFoundException.class, () -> userController
                .updateUser(-1L, new UpdateUserRequestDTO("update_user"))
        );

        assertEquals("User not found", exception.getMessage());
        exception = assertThrows(DataNotFoundException.class, () -> userController
                .updateUser(1L, new UpdateUserRequestDTO("update_user"))
        );
        assertEquals("City not found", exception.getMessage());
    }

    @Test
    void deleteUserTest() {
        User user = userRepository.save(User.builder()
                .username(testName)
                .email("user2@example.com")
                .passwordHash(testPassword)
                .roleId(1L)
                .build());

        userController.deleteUser(user.getId());
        Exception exception = assertThrows(UserNotFoundException.class, () -> userController
                .updateUser(user.getId(), new UpdateUserRequestDTO("update_user"))
        );

        assertEquals("User not found", exception.getMessage());
    }
}
