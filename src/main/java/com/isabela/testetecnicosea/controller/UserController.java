package com.isabela.testetecnicosea.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Usuários",
        description = "Cadastro e gerenciamento de usuários do sistema (CLIENT, ANALYST, ADMIN)"
)
@RestController
@RequestMapping("/users")
public class UserController {

    @Operation(
            summary = "Cria um usuário",
            description = "Cria e salva um novo usuário"
    )
    @PostMapping(
            path = ""
    )
    public ResponseEntity<String> create() {
        return ResponseEntity.ok("UserController CREATE está funcionando!");
    }


    @Operation(
            summary = "Atualiza um usuário",
            description = "Atualiza e salva os dados de um usuário"
    )
    @PutMapping (
            path = "/{id}"
    )
    public ResponseEntity<String> update(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("UserController UPDATE está funcionando!");
    }


    @Operation(
            summary = "Busca um determinado usuário",
            description = "Busca e retorna os dados de determinado usuário"
    )
    @GetMapping (
            path = "/{id}"
    )
    public ResponseEntity<String> read(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("UserController READ está funcionando!");
    }


    @Operation(
            summary = "Exclui um usuário"
    )
    @DeleteMapping (
            path = "/{id}"
    )
    public ResponseEntity<String> delete(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("UserController DELETE está funcionando! " + id);
    }


    @Operation(
            summary = "Lista todos os usuários cadastrados"
    )
    @GetMapping (
            path = ""
    )
    public ResponseEntity<String> list() {
        return ResponseEntity.ok("UserController LIST está funcionando!");
    }
}
