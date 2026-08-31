package com.isabela.testetecnicosea.controller;

import com.isabela.testetecnicosea.model.dto.SolicitationResponseDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep1RequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep2RequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep3RequestDTO;
import com.isabela.testetecnicosea.model.entity.Solicitation;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import com.isabela.testetecnicosea.model.mapper.SolicitationMapper;
import com.isabela.testetecnicosea.service.SolicitationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/solicitations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SolicitationController {

    private final SolicitationService solicitationService;
    private final SolicitationMapper solicitationMapper;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SolicitationResponseDTO> create(
            @AuthenticationPrincipal User client
    ) {
        Solicitation solicitation = solicitationService.create(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitationMapper.toResponse(solicitation));
    }


    @PutMapping(
            path = "/{id}/step1",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SolicitationResponseDTO> updateStep1(
            @PathVariable Integer id,
            @Valid @RequestBody SolicitationStep1RequestDTO request,
            @AuthenticationPrincipal User client
    ) {
        Solicitation solicitation = solicitationService.saveStep1(id, request, client);
        return ResponseEntity.ok(solicitationMapper.toResponse(solicitation));
    }


    @PutMapping(
            path = "/{id}/step2",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SolicitationResponseDTO> updateStep2(
            @PathVariable Integer id,
            @Valid @RequestBody SolicitationStep2RequestDTO request,
            @AuthenticationPrincipal User client
    ) {
        Solicitation solicitation = solicitationService.saveStep2(id, request, client);
        return ResponseEntity.ok(solicitationMapper.toResponse(solicitation));
    }


    @PutMapping(
            path = "/{id}/step3",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SolicitationResponseDTO> updateStep3(
            @PathVariable Integer id,
            @Valid @RequestBody SolicitationStep3RequestDTO request,
            @AuthenticationPrincipal User client
    ) {
        Solicitation solicitation = solicitationService.saveStep3(id, request, client);
        return ResponseEntity.ok(solicitationMapper.toResponse(solicitation));
    }


    @PostMapping(path = "/{id}/submit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SolicitationResponseDTO> submit(
            @PathVariable Integer id,
            @AuthenticationPrincipal User client
    ) {
        Solicitation solicitation = solicitationService.submit(id, client);
        return ResponseEntity.ok(solicitationMapper.toResponse(solicitation));
    }


    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SolicitationResponseDTO> getById(
            @PathVariable Integer id,
            @AuthenticationPrincipal User requester
    ) {
        Solicitation solicitation = solicitationService.findById(id, requester);
        return ResponseEntity.ok(solicitationMapper.toResponse(solicitation));
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SolicitationResponseDTO>> list(
            @AuthenticationPrincipal User client,
            @RequestParam(required = false) SolicitationStatus status
    ) {
        List<SolicitationResponseDTO> result = solicitationService.list(client, status)
                .stream()
                .map(solicitationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(result);
    }

}
