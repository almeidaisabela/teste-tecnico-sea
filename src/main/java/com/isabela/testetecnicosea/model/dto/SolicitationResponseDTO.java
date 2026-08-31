package com.isabela.testetecnicosea.model.dto;

import com.isabela.testetecnicosea.model.enums.Priority;
import com.isabela.testetecnicosea.model.enums.ServiceType;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import com.isabela.testetecnicosea.model.enums.State;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Schema(name = "SolicitationResponse", description = "Dados retornados de uma solicitação")
public record SolicitationResponseDTO(

        @Schema(description = "Identificador único da solicitação", example = "1")
        Integer id,

        @Schema(description = "Identificador do cliente dono da solicitação", example = "3")
        Integer clientId,

        @Schema(description = "Status atual da solicitação", example = "DRAFT")
        SolicitationStatus status,

        @Schema(description = "Etapa mais avançada já concluída (0 a 3)", example = "1")
        Integer currentStep,

        @Schema(description = "Tipo de serviço solicitado", example = "INSTALLATION")
        ServiceType serviceType,

        @Schema(description = "Título da solicitação", example = "Instalação de ar condicionado")
        String title,

        @Schema(description = "Descrição detalhada da solicitação", example = "Preciso instalar um ar condicionado split de 12000 BTUs na sala.")
        String description,

        @Schema(description = "CEP do endereço de atendimento", example = "01310-100")
        String cep,

        @Schema(description = "Número do endereço", example = "1578")
        String number,

        @Schema(description = "Complemento do endereço", example = "Apto 42")
        String complement,

        @Schema(description = "Logradouro, preenchido via consulta de CEP", example = "Avenida Paulista")
        String street,

        @Schema(description = "Bairro, preenchido via consulta de CEP", example = "Bela Vista")
        String neighborhood,

        @Schema(description = "Cidade, preenchida via consulta de CEP", example = "São Paulo")
        String city,

        @Schema(description = "UF, preenchida via consulta de CEP", example = "SP")
        State state,

        @Schema(description = "Prioridade da solicitação", example = "MEDIUM")
        Priority priority,

        @Schema(description = "Data preferida para atendimento", example = "2026-09-15")
        LocalDate preferredDate,

        @Schema(description = "Valor estimado do serviço", example = "250.00")
        BigDecimal estimatedValue,

        @Schema(description = "Indica se os termos foram aceitos", example = "true")
        Boolean termsAccepted,

        @Schema(description = "Data e horário de criação da solicitação")
        LocalDateTime createdAt,

        @Schema(description = "Data e horário da última atualização")
        LocalDateTime updatedAt,

        @Schema(description = "Data e horário do envio para análise")
        LocalDateTime submittedAt,

        @Schema(description = "Data e horário da análise")
        LocalDateTime analyzedAt,

        @Schema(description = "Identificador do analista responsável pela decisão", example = "2")
        Integer analyzedBy,

        @Schema(description = "Comentário da análise")
        String analysisComment

) {}
