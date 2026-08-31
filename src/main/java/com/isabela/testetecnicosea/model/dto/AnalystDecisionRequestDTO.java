package com.isabela.testetecnicosea.model.dto;

import com.isabela.testetecnicosea.model.enums.AnalystDecision;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "AnalystDecisionRequest", description = "Decisão do analista sobre uma solicitação")
public record AnalystDecisionRequestDTO(

        @NotNull(message = "O campo DECISÃO é obrigatório.")
        @Schema(example = "APPROVE")
        AnalystDecision decision,

        @NotBlank(message = "O campo COMENTÁRIO é obrigatório.")
        @Size(min = 10, max = 1000, message = "O COMENTÁRIO deve ter entre 10 e 1000 caracteres.")
        @Schema(example = "Documentação completa e endereço validado. Aprovado para execução.")
        String comment

) {}
