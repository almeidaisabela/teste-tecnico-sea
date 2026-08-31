package com.isabela.testetecnicosea.service;


import com.isabela.testetecnicosea.model.dto.ViaCepResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class CepService {

    private final RestClient restClient;

    public CepService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://viacep.com.br/ws")
                .build();
    }

    public ViaCepResponseDTO findAdress(String cep) {
        String cepLimpo = cep.replaceAll("\\D", "");

        if (cepLimpo.length() != 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP inválido: formato incorreto");
        }

        try {
            ViaCepResponseDTO response = restClient.get()
                    .uri("/{cep}/json", cepLimpo)
                    .retrieve()
                    .body(ViaCepResponseDTO.class);

            if (response == null || Boolean.TRUE.equals(response.erro())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP não encontrado");
            }
            return response;

        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("Falha ao consultar ViaCEP para o CEP {}: {}", cepLimpo, exception.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível validar o CEP no momento");
        }
    }

}
