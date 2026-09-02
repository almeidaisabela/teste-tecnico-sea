package com.isabela.testetecnicosea.model.dto;

import java.util.List;

public record SolicitationSearchResponseDTO(
        List<SolicitationResponseDTO> items,
        int page,
        int size,
        long total
) {}
