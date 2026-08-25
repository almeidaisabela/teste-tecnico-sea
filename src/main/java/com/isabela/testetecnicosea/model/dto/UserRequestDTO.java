package com.isabela.testetecnicosea.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Schema(name = "UsersDTO", description = "Dados para cadastro de um novo usuário")
public record UserRequestDTO(

        @Schema(name = "Id", example = "1")
        Long id,

        @NotBlank(message = "O campo NOME é obrigatório.")
        @Size(max = 120, message = "O campo NOME excede o limite de 120 caracteres.")
        @Schema(name = "name", example = "Ana Silva")
        String name,

        @NotBlank(message = "O campo EMAIL é obrigatório.")
        @Size(max = 100, message = "O campo EMAIL excede o limite de 100 caracteres.")
        @Schema(name = "email", example = "ana@gmail.com")
        String email,

        @NotBlank(message = "O campo SENHA é obrigatório.")
        @Size(max = 80, message = "O campo SENHA excede o limite de 80 caracteres.")
        @Schema(name = "password", example = "ABC123")
        String passwordHash,

        @NotNull(message = "O campo PERFIL é obrigatório.")
        @Schema(name = "role", example = "ADMIN")
        String role

) {}
