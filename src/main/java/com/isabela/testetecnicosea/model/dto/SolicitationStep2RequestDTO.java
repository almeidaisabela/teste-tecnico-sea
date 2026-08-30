package com.isabela.testetecnicosea.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Schema(name = "Step2Request", description = "Endereço da solicitação (Etapa 2)")
public record SolicitationStep2RequestDTO(

        @NotBlank(message = "O campo CEP é obrigatório.")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "O CEP deve estar no formato 00000-000 ou 00000000.")
        @Schema(example = "01310-100")
        String cep,

        @NotBlank(message = "O campo NÚMERO é obrigatório.")
        @Size(min = 1, max = 20, message = "O NÚMERO deve ter entre 1 e 20 caracteres.")
        @Schema(example = "1578")
        String number,

        @Size(max = 100, message = "O COMPLEMENTO excede o limite de 100 caracteres.")
        @Schema(example = "Apto 42")
        String complement

) {}
