package com.isabela.testetecnicosea.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/solicitations")
@RequiredArgsConstructor
public class SolicitationController {

    @PostMapping(
            path = ""
    )
    public ResponseEntity<String> register() {
        return ResponseEntity.ok("SolicitationController REGISTER está funcionando!");
    }

    @PutMapping(
            path = "/{id}/step1"
    )
    public ResponseEntity<String> update1() {
        return ResponseEntity.ok("SolicitationController UPDATE 1 está funcionando!");
    }

    @PutMapping(
            path = "/{id}/step2"
    )
    public ResponseEntity<String> update2() {
        return ResponseEntity.ok("SolicitationController UPDATE 2 está funcionando!");
    }

    @PutMapping(
            path = "/{id}/step3"
    )
    public ResponseEntity<String> update3() {
        return ResponseEntity.ok("SolicitationController UPDATE 3 está funcionando!");
    }

    @PostMapping(
            path = "/{id}/submit"
    )
    public ResponseEntity<String> create() {
        return ResponseEntity.ok("SolicitationController CREATE está funcionando!");
    }

    @GetMapping(
            path = "{id}"
    )
    public ResponseEntity<String> getById() {
        return ResponseEntity.ok("SolicitationController GetById está funcionando!");
    }

    @GetMapping(
            path = "/list"
    )
    public ResponseEntity<String> list() {
        return ResponseEntity.ok("SolicitationController LIST está funcionando!");
    }

}
