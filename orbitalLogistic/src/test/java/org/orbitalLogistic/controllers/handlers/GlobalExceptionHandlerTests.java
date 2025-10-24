package org.orbitalLogistic.controllers.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.dto.common.ErrorResponseDTO;
import org.orbitalLogistic.exceptions.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.validation.*;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTests {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    
    @Test
    void handleNotFound_ShouldReturn404() {
        var ex = new CargoNotFoundException("Груз не найден");
        ResponseEntity<ErrorResponseDTO> response = handler.handleNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().message().contains("не найден"));
    }

    
    @Test
    void handleConflict_ShouldReturn409() {
        var ex = new CargoAlreadyExistsException("Уже существует");
        ResponseEntity<ErrorResponseDTO> response = handler.handleConflictException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().message().contains("уже существует"));
    }

    
    @Test
    void handleValidation_ShouldReturn400_WithFieldErrors() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "test");
        bindingResult.addError(new FieldError("test", "name", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponseDTO> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ошибка валидации", response.getBody().error());
        assertTrue(response.getBody().details().containsKey("name"));
    }

    
    @Test
    void handleIllegalArgument_ShouldReturn400() {
        var ex = new IllegalArgumentException("Invalid format");
        ResponseEntity<ErrorResponseDTO> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("Неверный формат"));
    }

    
    @Test
    void handleHttpMessageNotReadable_ShouldReturn400() {
        var ex = new HttpMessageNotReadableException("Invalid JSON");
        ResponseEntity<ErrorResponseDTO> response = handler.handleHttpMessageNotReadableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ошибка формата данных", response.getBody().error());
    }

    
    @Test
    void handleTypeMismatch_ShouldReturn400() {
        var ex = new MethodArgumentTypeMismatchException("abc", Integer.class, "id", null, null);
        ResponseEntity<ErrorResponseDTO> response = handler.handleMethodArgumentTypeMismatchException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String msg = response.getBody().message();
        assertNotNull(msg);
        assertTrue(msg.toLowerCase().contains("параметр")); 
    }

    
    @Test
    void handleMissingParam_ShouldReturn400() throws Exception {
        var ex = new MissingServletRequestParameterException("page", "int");
        ResponseEntity<ErrorResponseDTO> response = handler.handleMissingServletRequestParameterException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("page"));
    }

    
    @Test
    void handleInvalidOperation_ShouldReturn400() {
        var ex = new InvalidOperationException("Недопустимо");
        ResponseEntity<ErrorResponseDTO> response = handler.handleInvalidOperationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("Операция не может"));
    }

    
    @Test
    void handleIllegalState_ShouldReturn409() {
        var ex = new IllegalStateException("Already exists");
        ResponseEntity<ErrorResponseDTO> response = handler.handleIllegalStateException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().message().contains("уже"));
    }

    
    @Test
    void handleDataIntegrityViolation_ForeignKey_ShouldReturn409() {
        var ex = new DataIntegrityViolationException("foreign key constraint fails");
        ResponseEntity<ErrorResponseDTO> response = handler.handleDataIntegrityViolationException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().message().contains("связана"));
    }

    @Test
    void handleDataIntegrityViolation_Unique_ShouldReturn409() {
        var ex = new DataIntegrityViolationException("unique constraint");
        ResponseEntity<ErrorResponseDTO> response = handler.handleDataIntegrityViolationException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().message().contains("уже существует"));
    }










    
    @Test
    void handleNoHandlerFound_ShouldReturn404() {
        var ex = new NoHandlerFoundException("GET", "/api/test", null);
        ResponseEntity<ErrorResponseDTO> response = handler.handleNoHandlerFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().message().contains("/api/test"));
    }

    
    @Test
    void handleGeneric_ShouldReturn500_DefaultProfile() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleGenericException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Внутренняя ошибка сервера", response.getBody().error());
        assertNull(response.getBody().details());
    }

    @Test
    void handleGeneric_ShouldIncludeDetails_WhenDevProfile() throws Exception {
        
        var field = GlobalExceptionHandler.class.getDeclaredField("activeProfile");
        field.setAccessible(true);
        field.set(handler, "dev");

        ResponseEntity<ErrorResponseDTO> response = handler.handleGenericException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody().details());
        assertTrue(response.getBody().details().get("technicalDetails").contains("RuntimeException"));
    }
}
