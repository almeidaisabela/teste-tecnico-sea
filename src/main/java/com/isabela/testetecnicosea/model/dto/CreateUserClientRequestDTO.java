package com.isabela.testetecnicosea.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateUserClientRequestDTO", description = "Dados para autocadastro de cliente")
public record CreateUserClientRequestDTO(

        @NotBlank(message = "O campo NOME é obrigatório.")
        @Size(max = 120, message = "O campo NOME excede o limite de 120 caracteres.")
        @Schema(example = "José Silva")
        String name,

        @NotBlank(message = "O campo EMAIL é obrigatório.")
        @Email(message = "O campo EMAIL deve ser um e-mail válido.")
        @Size(max = 100, message = "O campo EMAIL excede o limite de 100 caracteres.")
        @Schema(example = "user@email.com")
        String email,

        @NotBlank(message = "O campo SENHA é obrigatório.")
        @Size(min = 8, max = 80, message = "A SENHA deve ter entre 8 e 80 caracteres.")
        @Schema(example = "senha123")
        String password
) {}
