package com.isabela.testetecnicosea.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponseDTO", description = "Resposta da autenticação bem-sucedida")
public record LoginResponseDTO (

        @Schema(description = "Token JWT de acesso")
        String token,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType
) {}