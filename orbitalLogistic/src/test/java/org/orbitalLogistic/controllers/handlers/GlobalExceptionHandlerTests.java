package org.orbitalLogistic.controllers.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.exceptions.*;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.exceptions.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTests {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleUserNotFoundException_ShouldReturnNotFoundResponse() {

        UserNotFoundException exception = new UserNotFoundException("User not found with id: 1");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleUserNotFoundException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("User not found with id: 1", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleStorageUnitNotFoundException_ShouldReturnNotFoundResponse() {

        StorageUnitNotFoundException exception = new StorageUnitNotFoundException("Storage unit not found");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleStorageUnitNotFoundException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Storage unit not found", response.getBody().message());
    }

    @Test
    void handleSpacecraftNotFoundException_ShouldReturnNotFoundResponse() {

        SpacecraftNotFoundException exception = new SpacecraftNotFoundException("Spacecraft not found");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleSpacecraftNotFoundException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Spacecraft not found", response.getBody().message());
    }

    @Test
    void handleCargoCategoryNotFoundException_ShouldReturnNotFoundResponse() {

        CargoCategoryNotFoundException exception = new CargoCategoryNotFoundException("Cargo category not found");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleCargoCategoryNotFoundException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Cargo category not found", response.getBody().message());
    }

    @Test
    void handleCargoManifestNotFoundException_ShouldReturnNotFoundResponse() {

        CargoManifestNotFoundException exception = new CargoManifestNotFoundException("Cargo manifest not found");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleCargoManifestNotFoundException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Cargo manifest not found", response.getBody().message());
    }

    @Test
    void handleMissionNotFoundException_ShouldReturnNotFoundResponse() {

        MissionNotFoundException exception = new MissionNotFoundException("Mission not found");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleMissionNotFoundException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Mission not found", response.getBody().message());
    }

    @Test
    void handleInventoryTransactionNotFoundException_ShouldReturnNotFoundResponse() {

        InventoryTransactionNotFoundException exception = new InventoryTransactionNotFoundException("Transaction not found");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleInventoryTransactionNotFoundException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Transaction not found", response.getBody().message());
    }

    @Test
    void handleUserAlreadyExistsException_ShouldReturnConflictResponse() {

        UserAlreadyExistsException exception = new UserAlreadyExistsException("User already exists");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleUserAlreadyExistsException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals("User already exists", response.getBody().message());
    }

    @Test
    void handleMissionAlreadyExistsException_ShouldReturnConflictResponse() {

        MissionAlreadyExistsException exception = new MissionAlreadyExistsException("Mission already exists");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleMissionAlreadyExistsException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Mission already exists", response.getBody().message());
    }

    @Test
    void handleStorageUnitAlreadyExistsException_ShouldReturnConflictResponse() {

        StorageUnitAlreadyExistsException exception = new StorageUnitAlreadyExistsException("Storage unit already exists");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleStorageUnitAlreadyExistsException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Storage unit already exists", response.getBody().message());
    }

    @Test
    void handleSpacecraftAlreadyExistsException_ShouldReturnConflictResponse() {
        SpacecraftAlreadyExistsException exception = new SpacecraftAlreadyExistsException("Spacecraft already exists");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleSpacecraftAlreadyExistsException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Spacecraft already exists", response.getBody().message());
    }

    @Test
    void handleDataNotFoundException_ShouldReturnNotFoundResponse() {

        DataNotFoundException exception = new DataNotFoundException("Data not found");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleDataNotFoundException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Data not found", response.getBody().message());
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequestWithDetails() {

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "email", "Email must be valid");
        FieldError fieldError2 = new FieldError("object", "password", "Password must be at least 8 characters");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));


        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> response =
                exceptionHandler.handleValidationExceptions(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Validation failed", response.getBody().message());

        Map<String, String> details = response.getBody().details();
        assertNotNull(details);
        assertEquals(2, details.size());
        assertEquals("Email must be valid", details.get("email"));
        assertEquals("Password must be at least 8 characters", details.get("password"));
    }

    @Test
    void handleValidationExceptions_WithEmptyErrors_ShouldReturnBadRequest() {

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList());


        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> response =
                exceptionHandler.handleValidationExceptions(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().details());
        assertTrue(response.getBody().details().isEmpty());
    }

    @Test
    void handleAllUncaughtException_ShouldReturnInternalServerError() {

        Exception exception = new RuntimeException("Unexpected error");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleAllUncaughtException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }

    @Test
    void handleAllUncaughtException_WithNullPointerException_ShouldReturnInternalServerError() {

        NullPointerException exception = new NullPointerException("Null pointer");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleAllUncaughtException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }

    @Test
    void errorResponseRecord_ShouldHaveCorrectStructure() {

        LocalDateTime timestamp = LocalDateTime.now();
        GlobalExceptionHandler.ErrorResponse errorResponse =
                new GlobalExceptionHandler.ErrorResponse(timestamp, 404, "Not Found", "Resource not found");


        assertEquals(timestamp, errorResponse.timestamp());
        assertEquals(404, errorResponse.status());
        assertEquals("Not Found", errorResponse.error());
        assertEquals("Resource not found", errorResponse.message());
    }

    @Test
    void validationErrorResponseRecord_ShouldHaveCorrectStructure() {

        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> details = new HashMap<>();
        details.put("field", "error message");

        GlobalExceptionHandler.ValidationErrorResponse validationErrorResponse =
                new GlobalExceptionHandler.ValidationErrorResponse(
                        timestamp, 400, "Bad Request", "Validation failed", details);


        assertEquals(timestamp, validationErrorResponse.timestamp());
        assertEquals(400, validationErrorResponse.status());
        assertEquals("Bad Request", validationErrorResponse.error());
        assertEquals("Validation failed", validationErrorResponse.message());
        assertEquals(details, validationErrorResponse.details());
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnInternalServerError() {

        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleAllUncaughtException(exception);


        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleDifferentNotFoundExceptions_ShouldAllReturn404() {

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> userNotFoundResponse =
                exceptionHandler.handleUserNotFoundException(new UserNotFoundException("User not found"));
        assertEquals(HttpStatus.NOT_FOUND, userNotFoundResponse.getStatusCode());

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> storageUnitNotFoundResponse =
                exceptionHandler.handleStorageUnitNotFoundException(new StorageUnitNotFoundException("Storage unit not found"));
        assertEquals(HttpStatus.NOT_FOUND, storageUnitNotFoundResponse.getStatusCode());

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> dataNotFoundResponse =
                exceptionHandler.handleDataNotFoundException(new DataNotFoundException("Data not found"));
        assertEquals(HttpStatus.NOT_FOUND, dataNotFoundResponse.getStatusCode());
    }

    @Test
    void handleDifferentConflictExceptions_ShouldAllReturn409() {

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> userConflictResponse =
                exceptionHandler.handleUserAlreadyExistsException(new UserAlreadyExistsException("User exists"));
        assertEquals(HttpStatus.CONFLICT, userConflictResponse.getStatusCode());

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> storageUnitConflictResponse =
                exceptionHandler.handleStorageUnitAlreadyExistsException(new StorageUnitAlreadyExistsException("Storage unit exists"));
        assertEquals(HttpStatus.CONFLICT, storageUnitConflictResponse.getStatusCode());

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> missionConflictResponse =
                exceptionHandler.handleMissionAlreadyExistsException(new MissionAlreadyExistsException("Mission exists"));
        assertEquals(HttpStatus.CONFLICT, missionConflictResponse.getStatusCode());
    }

    @Test
    void errorResponseTimestamps_ShouldBeRecent() {

        UserNotFoundException exception = new UserNotFoundException("Test");


        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleUserNotFoundException(exception);


        assertNotNull(response.getBody().timestamp());

        assertTrue(LocalDateTime.now().minusSeconds(1).isBefore(response.getBody().timestamp()));
    }
}