package com.isabela.testetecnicosea.model.dto;

import com.isabela.testetecnicosea.model.enums.Priority;
import com.isabela.testetecnicosea.model.enums.ServiceType;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import com.isabela.testetecnicosea.model.enums.State;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
import java.util.List;

public record SolicitationSearchRequestDTO(

        @Schema(description = "Busca textual em title e description")
        String q,

        @Schema(description = "Filtro por um ou mais status")
        List<SolicitationStatus> status,

        @Schema(description = "Filtro por tipo de serviço")
        ServiceType serviceType,

        @Schema(description = "Filtro por prioridade")
        Priority priority,

        @Schema(description = "Filtro por UF (para ANALYST é forçado ao coverage)")
        State state,

        @Schema(description = "Data inicial (submittedAt)")
        LocalDateTime dateFrom,

        @Schema(description = "Data final (submittedAt)")
        LocalDateTime dateTo,

        @Min(value = 0, message = "page deve ser >= 0")
        Integer page,

        @Min(value = 1, message = "size deve ser >= 1")
        Integer size,

        @Schema(description = "Formato: campo,direção. Ex: submittedAt,desc")
        String sort

) {}
