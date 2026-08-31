package com.isabela.testetecnicosea.model.dto;

public record ViaCepResponseDTO(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        Boolean erro
) {}
