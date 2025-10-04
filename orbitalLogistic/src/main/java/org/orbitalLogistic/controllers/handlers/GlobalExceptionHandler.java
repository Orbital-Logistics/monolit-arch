package org.orbitalLogistic.controllers.handlers;

import org.orbitalLogistic.dto.common.ErrorResponseDTO;
import org.orbitalLogistic.exceptions.CargoAlreadyExistsException;
import org.orbitalLogistic.exceptions.CargoNotFoundException;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.orbitalLogistic.exceptions.SpacecraftAlreadyExistsException;
import org.orbitalLogistic.exceptions.SpacecraftNotFoundException;
import org.orbitalLogistic.exceptions.StorageUnitAlreadyExistsException;
import org.orbitalLogistic.exceptions.StorageUnitNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        SpacecraftNotFoundException.class,
        CargoNotFoundException.class,
        StorageUnitNotFoundException.class,
        DataNotFoundException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleNotFoundException(RuntimeException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler({
        SpacecraftAlreadyExistsException.class,
        CargoAlreadyExistsException.class,
        StorageUnitAlreadyExistsException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleConflictException(RuntimeException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Invalid input data",
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
