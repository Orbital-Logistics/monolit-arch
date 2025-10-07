package org.orbitalLogistic.controllers.handlers;

import org.orbitalLogistic.dto.common.ErrorResponseDTO;
import org.orbitalLogistic.exceptions.*;
import org.orbitalLogistic.exceptions.common.DataNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @ExceptionHandler({
        SpacecraftNotFoundException.class,
        CargoNotFoundException.class,
        StorageUnitNotFoundException.class,
        MissionNotFoundException.class,
        CargoManifestNotFoundException.class,
        CargoStorageNotFoundException.class,
        MissionAssignmentNotFoundException.class,
        CargoCategoryNotFoundException.class,
        MaintenanceLogNotFoundException.class,
        SpacecraftTypeNotFoundException.class,
        InventoryTransactionNotFoundException.class,
        DataNotFoundException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleNotFoundException(RuntimeException ex) {
        String humanReadableMessage = getHumanReadableNotFoundMessage(ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Ресурс не найден",
            humanReadableMessage
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler({
        SpacecraftAlreadyExistsException.class,
        CargoAlreadyExistsException.class,
        StorageUnitAlreadyExistsException.class,
        MissionAlreadyExistsException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleConflictException(RuntimeException ex) {
        String humanReadableMessage = getHumanReadableConflictMessage(ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Конфликт данных",
            humanReadableMessage
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

        String mainMessage = "Проверьте правильность заполнения полей";
        if (errors.size() == 1) {
            mainMessage = "Поле заполнено некорректно: " + errors.values().iterator().next();
        }

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ошибка валидации",
            mainMessage,
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        String humanReadableMessage = getHumanReadableBadRequestMessage(ex.getMessage());

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Некорректные данные",
            humanReadableMessage
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ошибка формата данных",
            "Проверьте правильность формата JSON и типы данных в запросе"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Параметр '%s' должен быть типа %s",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "неизвестный");

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Неверный тип параметра",
            message
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String message = String.format("Обязательный параметр '%s' отсутствует", ex.getParameterName());

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Отсутствует параметр",
            message
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidOperationException(InvalidOperationException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Недопустимая операция",
            "Операция не может быть выполнена: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalStateException(IllegalStateException ex) {
        String humanReadableMessage = getHumanReadableStateMessage(ex.getMessage());

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Неверное состояние",
            humanReadableMessage
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String humanReadableMessage = "Операция нарушает целостность данных. Возможно, запись используется в других местах или нарушены ограничения базы данных.";

        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("foreign key")) {
                humanReadableMessage = "Невозможно выполнить операцию, так как запись связана с другими данными в системе";
            } else if (ex.getMessage().contains("unique")) {
                humanReadableMessage = "Запись с такими данными уже существует в системе";
            }
        }

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Нарушение целостности данных",
            humanReadableMessage
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        String message = String.format("HTTP метод '%s' не поддерживается для данного URL. Поддерживаемые методы: %s",
            ex.getMethod(),
            String.join(", ", ex.getSupportedMethods()));

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            "Метод не поддерживается",
            message
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        String message = String.format("Endpoint '%s %s' не найден", ex.getHttpMethod(), ex.getRequestURL());

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Endpoint не найден",
            message
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        String message = "Произошла внутренняя ошибка сервера";
        String detailedMessage = null;

        if ("docker".equals(activeProfile) || "dev".equals(activeProfile) || "development".equals(activeProfile)) {
            detailedMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            ex.printStackTrace();
        }

        Map<String, String> details = null;
        if (detailedMessage != null) {
            details = Map.of("technicalDetails", detailedMessage);
        }

        ErrorResponseDTO error = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Внутренняя ошибка сервера",
            message,
            details
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private String getHumanReadableNotFoundMessage(RuntimeException ex) {
        String className = ex.getClass().getSimpleName();

        return switch (className) {
            case "SpacecraftNotFoundException" -> "Космический корабль не найден";
            case "CargoNotFoundException" -> "Груз не найден";
            case "StorageUnitNotFoundException" -> "Складской модуль не найден";
            case "MissionNotFoundException" -> "Миссия не найдена";
            case "CargoManifestNotFoundException" -> "Грузовой манифест не найден";
            case "CargoStorageNotFoundException" -> "Запись о хранении груза не найдена";
            case "MissionAssignmentNotFoundException" -> "Назначение на миссию не найдено";
            case "CargoCategoryNotFoundException" -> "Категория груза не найдена";
            case "MaintenanceLogNotFoundException" -> "Запись о техническом обслуживании не найдена";
            case "SpacecraftTypeNotFoundException" -> "Тип космического корабля не найден";
            case "InventoryTransactionNotFoundException" -> "Транзакция инвентаря не найдена";
            default -> "Запрашиваемый ресурс не найден";
        };
    }

    private String getHumanReadableConflictMessage(RuntimeException ex) {
        String className = ex.getClass().getSimpleName();

        return switch (className) {
            case "SpacecraftAlreadyExistsException" -> "Космический корабль с такими данными уже существует";
            case "CargoAlreadyExistsException" -> "Груз с такими данными уже существует";
            case "StorageUnitAlreadyExistsException" -> "Складской модуль с такими данными уже существует";
            case "MissionAlreadyExistsException" -> "Миссия с такими данными уже существует";
            default -> "Запись с такими данными уже существует в системе";
        };
    }

    private String getHumanReadableBadRequestMessage(String originalMessage) {
        if (originalMessage == null) return "Некорректные входные данные";

        if (originalMessage.toLowerCase().contains("required")) {
            return "Отсутствует обязательное поле или параметр";
        }
        if (originalMessage.toLowerCase().contains("format")) {
            return "Неверный формат данных";
        }
        if (originalMessage.toLowerCase().contains("range")) {
            return "Значение выходит за допустимые пределы";
        }

        return originalMessage;
    }

    private String getHumanReadableStateMessage(String originalMessage) {
        if (originalMessage == null) return "Операция невозможна в текущем состоянии объекта";

        if (originalMessage.toLowerCase().contains("already")) {
            return "Операция уже выполнена или объект уже находится в требуемом состоянии";
        }
        if (originalMessage.toLowerCase().contains("status")) {
            return "Операция невозможна из-за текущего статуса объекта";
        }

        return originalMessage;
    }
}
