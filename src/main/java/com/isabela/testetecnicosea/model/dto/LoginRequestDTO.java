package com.isabela.testetecnicosea.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "LoginRequestDTO", description = "Credenciais para autenticação")
public record LoginRequestDTO (

        @NotBlank(message = "O campo EMAIL é obrigatório.")
        @Email(message = "O campo EMAIL deve ser um e-mail válido.")
        @Schema(example = "user@email.com")
        String email,

        @NotBlank(message = "O campo SENHA é obrigatório.")
        @Size(min = 8, max = 80, message = "A SENHA deve ter entre 8 e 80 caracteres.")
        @Schema(example = "senha123")
        String password
) {}
