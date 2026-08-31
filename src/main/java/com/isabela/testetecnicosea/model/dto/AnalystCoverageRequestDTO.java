package com.isabela.testetecnicosea.model.dto;

import com.isabela.testetecnicosea.model.enums.State;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;


@Schema(name = "AnalystCoverageRequest", description = "Lista de UFs que um analista deve cobrir")
public record AnalystCoverageRequestDTO(

        @NotEmpty(message = "É necessário informar ao menos uma UF.")
        @Schema(description = "Lista de UFs")
        List<State> states

) {}
