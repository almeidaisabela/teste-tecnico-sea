package com.isabela.testetecnicosea.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "UserResponse", description = "Dados retornados de um usuário")
public record UserResponseDTO (

        @Schema(description = "Identificador único do usuário", example = "1")
        Long id,

        @Schema(description = "Nome completo do usuário", example = "Ana Silva")
        String name,

        @Schema(description = "E-mail do usuário", example = "ana@gmail.com")
        String email,

        @Schema(description = "Perfil de acesso do usuário", example = "ADMIN")
        String role,

        @Schema(description = "Indica se o usuário está habilitado")
        Boolean enabled,

        @Schema(description = "Data e horário de criação do usuário")
        LocalDateTime createdAt

) {}
