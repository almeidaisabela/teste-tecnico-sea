package com.isabela.testetecnicosea.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(name = "FieldError", description = "Erro de validação em um campo específico")
public record FieldErrorDTO(

        @Schema(description = "Nome do campo com erro")
        String field,

        @Schema(description = "Mensagem de validação")
        String message

) {}
