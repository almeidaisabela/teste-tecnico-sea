package com.isabela.testetecnicosea.model.mapper;

import com.isabela.testetecnicosea.model.dto.CreateInternalUserRequestDTO;
import com.isabela.testetecnicosea.model.dto.CreateUserClientRequestDTO;
import com.isabela.testetecnicosea.model.dto.UserResponseDTO;
import com.isabela.testetecnicosea.model.enums.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.isabela.testetecnicosea.model.entity.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    User mapToEntity(CreateUserClientRequestDTO request);

    default User toEntity(CreateUserClientRequestDTO request, String passwordHash) {
        User user = mapToEntity(request);
        user.setPasswordHash(passwordHash);
        user.setRole(UserRole.CLIENT);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    User mapToEntity(CreateInternalUserRequestDTO request);

    default User toEntity(CreateInternalUserRequestDTO request, String passwordHash) {
        User user = mapToEntity(request);
        user.setPasswordHash(passwordHash);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    UserResponseDTO toResponse(User user);

}
