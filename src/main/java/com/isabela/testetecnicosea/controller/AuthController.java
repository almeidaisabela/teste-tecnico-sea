package com.isabela.testetecnicosea.controller;

import com.isabela.testetecnicosea.model.dto.CreateUserClientRequestDTO;
import com.isabela.testetecnicosea.model.dto.LoginRequestDTO;
import com.isabela.testetecnicosea.model.dto.LoginResponseDTO;
import com.isabela.testetecnicosea.model.dto.UserResponseDTO;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.mapper.UserMapper;
import com.isabela.testetecnicosea.service.AuthService;
import com.isabela.testetecnicosea.service.TokenService;
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
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping(
            path = "/register",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody CreateUserClientRequestDTO createUserClientRequestDTO
    ) {
        User user = authService.register(createUserClientRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO
    ) {
        String token = authService.login(loginRequestDTO);
        return ResponseEntity.ok(new LoginResponseDTO(token, "Bearer"));
    }

}
