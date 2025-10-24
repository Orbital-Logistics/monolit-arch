package org.orbitalLogistic.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.orbitalLogistic.dto.request.UserRegistrationRequestDTO;
import org.orbitalLogistic.dto.response.UserResponseDTO;
import org.orbitalLogistic.entities.User;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // Entity -> Response DTO
    UserResponseDTO toResponseDTO(
            User user
    );

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roleId", ignore = true)
    User toEntity(UserRegistrationRequestDTO request);

    List<UserResponseDTO> toResponseDTOList(List<User> users);
}   
