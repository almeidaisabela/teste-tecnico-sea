package com.isabela.testetecnicosea.controller;

import com.isabela.testetecnicosea.model.dto.AnalystDecisionRequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationResponseDTO;
import com.isabela.testetecnicosea.model.entity.Solicitation;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.mapper.SolicitationMapper;
import com.isabela.testetecnicosea.service.AnalystSolicitationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analyst/solicitations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AnalystController {

    private final AnalystSolicitationService analystSolicitationService;
    private final SolicitationMapper solicitationMapper;


    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SolicitationResponseDTO> getById(
            @PathVariable Integer id,
            @AuthenticationPrincipal User analyst
    ) {
        Solicitation solicitation = analystSolicitationService.findById(id, analyst);
        return ResponseEntity.ok(solicitationMapper.toResponse(solicitation));
    }

    @PostMapping(path = "/{id}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SolicitationResponseDTO> start(
            @PathVariable Integer id,
            @AuthenticationPrincipal User analyst
    ) {
        Solicitation solicitation = analystSolicitationService.start(id, analyst);
        return ResponseEntity.ok(solicitationMapper.toResponse(solicitation));
    }


    @PostMapping(
            path = "/{id}/decide",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SolicitationResponseDTO> decide(
            @PathVariable Integer id,
            @Valid @RequestBody AnalystDecisionRequestDTO request,
            @AuthenticationPrincipal User analyst
    ) {
        Solicitation solicitation = analystSolicitationService.decide(id, request, analyst);
        return ResponseEntity.ok(solicitationMapper.toResponse(solicitation));
    }

}
