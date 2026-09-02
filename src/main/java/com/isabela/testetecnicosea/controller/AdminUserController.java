package com.isabela.testetecnicosea.controller;

import com.isabela.testetecnicosea.aop.Audit;
import com.isabela.testetecnicosea.model.dto.AnalystCoverageRequestDTO;
import com.isabela.testetecnicosea.model.dto.AnalystCoverageResponseDTO;
import com.isabela.testetecnicosea.model.dto.CreateInternalUserRequestDTO;
import com.isabela.testetecnicosea.model.dto.UserResponseDTO;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.mapper.UserMapper;
import com.isabela.testetecnicosea.service.AdminUserService;
import com.isabela.testetecnicosea.service.SolicitationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserMapper userMapper;
    private final SolicitationService solicitationService;

    @Audit(action = "CREATE_USER")
    @PostMapping(
            path = "/users",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserResponseDTO> createInternalUser(
            @RequestBody @Valid CreateInternalUserRequestDTO internalUserRequestDTO
    ) {
        User user = adminUserService.createInternalUser(internalUserRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
    }


    @PutMapping(
            path = "/users/{id}/coverage",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AnalystCoverageResponseDTO> updateCoverage(
            @PathVariable Integer id,
            @Valid @RequestBody AnalystCoverageRequestDTO request
    ) {
        AnalystCoverageResponseDTO response = adminUserService.updateCoverage(id, request);
        return ResponseEntity.ok(response);
    }


    @GetMapping(path = "/users/{id}/coverage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnalystCoverageResponseDTO> getCoverage(
            @PathVariable Integer id
    ) {
        AnalystCoverageResponseDTO response = adminUserService.getCoverage(id);
        return ResponseEntity.ok(response);
    }


}
