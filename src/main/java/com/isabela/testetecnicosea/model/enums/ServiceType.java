package com.isabela.testetecnicosea.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;


@Getter
@Schema(enumAsRef = true, description = "Tipos de serviço requisitados nas solicitações")
public enum ServiceType {
    INSTALLATION("Instalação"),
    MAINTENANCE("Manutenção"),
    INSPECTION("Inspeção");

    private final String service;
    ServiceType(String service) { this.service = service; }
}
