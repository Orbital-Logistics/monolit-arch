package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.orbitalLogistic.dto.common.ErrorResponseDTO;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ErrorMapper {

    default ErrorResponseDTO toErrorResponseDTO(
            String message,
            String code,
            String path
    ) {
        return new ErrorResponseDTO(
                message,
                code,
                LocalDateTime.now(),
                path,
                null
        );
    }

    default ErrorResponseDTO toValidationErrorResponseDTO(
            Map<String, String> fieldErrors,
            String path
    ) {
        return new ErrorResponseDTO(
                "Validation failed",
                "VALIDATION_ERROR",
                LocalDateTime.now(),
                path,
                fieldErrors
        );
    }
}
