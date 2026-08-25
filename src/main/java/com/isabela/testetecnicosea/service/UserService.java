package com.isabela.testetecnicosea.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    public String create() {
        log.info("Método create() do UserService foi chamado");
        return "UserService CREATE está funcionando!";
    }

    public String update(Long id) {
        log.info("Método update() do UserService foi chamado para o id {}", id);
        return "UserService UPDATE está funcionando!";
    }

    public String read(Long id) {
        log.info("Método read() do UserService foi chamado para o id {}", id);
        return "UserService READ está funcionando!";
    }

    public String delete(Long id) {
        log.info("Método delete() do UserService foi chamado para o id {}", id);
        return "UserService DELETE está funcionando!";
    }

    public String list() {
        log.info("Método list() do UserService foi chamado");
        return "UserService LIST está funcionando!";
    }
}
