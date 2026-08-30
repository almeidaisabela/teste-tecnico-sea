package com.isabela.testetecnicosea.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;


@Getter
@Schema(enumAsRef = true, description = "Status das solicitações")
public enum SolicitationStatus {

    DRAFT("Rascunho"),
    SUBMITTED("Enviado"),
    IN_REVIEW("Em análise"),
    APPROVED("Aprovado"),
    REJECTED("Rejeitado");

    private final String status;
    SolicitationStatus(String status) { this.status = status; }
}
