package com.isabela.testetecnicosea.model.dto;

import com.isabela.testetecnicosea.model.enums.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Schema(name = "Step1Request", description = "Dados básicos da solicitação (Etapa 1)")
public record SolicitationStep1RequestDTO(

        @NotNull(message = "O campo TIPO DE SERVIÇO é obrigatório.")
        @Schema(example = "INSTALLATION")
        ServiceType serviceType,

        @NotBlank(message = "O campo TÍTULO é obrigatório.")
        @Size(min = 3, max = 80, message = "O TÍTULO deve ter entre 3 e 80 caracteres.")
        @Schema(example = "Instalação de ar condicionado")
        String title,

        @NotBlank(message = "O campo DESCRIÇÃO é obrigatório.")
        @Size(min = 20, max = 1000, message = "A DESCRIÇÃO deve ter entre 20 e 1000 caracteres.")
        @Schema(example = "Preciso instalar um ar condicionado split de 12000 BTUs na sala de estar.")
        String description

) {}
