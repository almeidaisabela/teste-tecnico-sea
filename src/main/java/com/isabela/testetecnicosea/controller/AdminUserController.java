package com.isabela.testetecnicosea.controller;

import com.isabela.testetecnicosea.model.dto.CreateInternalUserRequestDTO;
import com.isabela.testetecnicosea.model.dto.UserResponseDTO;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.mapper.UserMapper;
import com.isabela.testetecnicosea.service.AdminUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserMapper userMapper;

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

}
