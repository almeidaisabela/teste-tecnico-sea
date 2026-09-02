package com.isabela.testetecnicosea.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;


@Schema(name = "ErrorResponse", description = "Formato padrão de erro da API")
public record ErrorResponseDTO(

        @Schema(description = "Momento em que o erro ocorreu")
        LocalDateTime timestamp,

        @Schema(description = "Código HTTP do erro")
        int status,

        @Schema(description = "Nome do erro HTTP (ex: BAD_REQUEST)")
        String error,

        @Schema(description = "Mensagem descritiva do erro")
        String message,

        @Schema(description = "Caminho da requisição que gerou o erro")
        String path,

        @Schema(description = "Lista de erros de validação por campo, quando aplicável")
        List<FieldErrorDTO> fieldErrors

) {}
