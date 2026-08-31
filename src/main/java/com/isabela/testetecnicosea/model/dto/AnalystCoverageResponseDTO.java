package com.isabela.testetecnicosea.model.dto;

import com.isabela.testetecnicosea.model.enums.State;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;


@Schema(name = "AnalystCoverageResponse", description = "UFs cobertas por um analista")
public record AnalystCoverageResponseDTO(

        @Schema(description = "Identificador do analista", example = "5")
        Integer userId,

        @Schema(description = "Lista de UFs cobertas")
        List<State> states

) {}
