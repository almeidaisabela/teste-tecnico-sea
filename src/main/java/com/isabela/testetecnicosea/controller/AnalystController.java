package com.isabela.testetecnicosea.controller;

import com.isabela.testetecnicosea.model.dto.AnalystDecisionRequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationResponseDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationSearchRequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationSearchResponseDTO;
import com.isabela.testetecnicosea.model.entity.Solicitation;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.Priority;
import com.isabela.testetecnicosea.model.enums.ServiceType;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import com.isabela.testetecnicosea.model.enums.State;
import com.isabela.testetecnicosea.model.mapper.SolicitationMapper;
import com.isabela.testetecnicosea.service.AnalystSolicitationService;
import com.isabela.testetecnicosea.service.SolicitationSearchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/analyst/solicitations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AnalystController {

    private final AnalystSolicitationService analystSolicitationService;
    private final SolicitationMapper solicitationMapper;
    private final SolicitationSearchService solicitationSearchService;


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


    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SolicitationSearchResponseDTO> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<SolicitationStatus> status,
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) State state,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal User requester
    ) {
        SolicitationSearchRequestDTO request = new SolicitationSearchRequestDTO(
                q, status, serviceType, priority, state, dateFrom, dateTo, page, size, sort
        );
        SolicitationSearchResponseDTO response = solicitationSearchService.search(request, requester);
        return ResponseEntity.ok(response);
    }

}
