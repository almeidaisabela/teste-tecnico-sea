package com.isabela.testetecnicosea.model.mapper;

import com.isabela.testetecnicosea.model.dto.UserRequestDTO;
import com.isabela.testetecnicosea.model.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.isabela.testetecnicosea.model.entity.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    User mapToEntity(UserRequestDTO request);

    UserResponseDTO toResponse(User user);

    default User toEntity(UserRequestDTO request, String passwordHash) {
        User user = mapToEntity(request);
        user.setPasswordHash(passwordHash);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
