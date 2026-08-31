package com.isabela.testetecnicosea.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ViaCepResponseDTO(

        @Schema(description = "Logradouro (rua/avenida)", example = "Avenida Paulista")
        String logradouro,

        @Schema(description = "Bairro", example = "Bela Vista")
        String bairro,

        @Schema(description = "Cidade", example = "São Paulo")
        String localidade,

        @Schema(description = "UF", example = "SP")
        String uf,

        @Schema(description = "Indica se o CEP consultado não foi encontrado")
        Boolean erro
) {}
