package com.isabela.testetecnicosea.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;


@Getter
@Schema(enumAsRef = true, description = "Prioridade das solicitações")
public enum Priority {
    LOW("Baixo"),
    MEDIUM("Médio"),
    HIGH("Alto");

    private final String priority;
    Priority(String priority) { this.priority = priority;}
}
