package com.isabela.testetecnicosea.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(enumAsRef = true, description = "Decisão do analista sobre as solicitações")
public enum AnalystDecision {

    APPROVE("Aprovar"),
    REJECT("Rejeitar");

    private final String decision;
    AnalystDecision(String decision) { this.decision = decision; }

}
