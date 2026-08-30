package com.isabela.testetecnicosea.model.dto;

import com.isabela.testetecnicosea.model.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;


@Schema(name = "Step3Request", description = "Confirmação e dados finais da solicitação (Etapa 3)")
public record SolicitationStep3RequestDTO(

        @NotNull(message = "O campo PRIORIDADE é obrigatório.")
        @Schema(example = "MEDIUM")
        Priority priority,

        @NotNull(message = "O campo DATA PREFERIDA é obrigatório.")
        @FutureOrPresent(message = "A DATA PREFERIDA não pode ser no passado.")
        @Schema(example = "2026-09-15")
        LocalDate preferredDate,

        @NotNull(message = "O campo VALOR ESTIMADO é obrigatório.")
        @DecimalMin(value = "0.0", inclusive = true, message = "O VALOR ESTIMADO deve ser maior ou igual a zero.")
        @Schema(example = "250.00")
        BigDecimal estimatedValue,

        @NotNull(message = "É necessário aceitar os termos.")
        @AssertTrue(message = "É necessário aceitar os termos para continuar.")
        @Schema(example = "true")
        Boolean termsAccepted

) {}
