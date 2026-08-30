package com.isabela.testetecnicosea.service;

import com.isabela.testetecnicosea.model.dto.CreateUserClientRequestDTO;
import com.isabela.testetecnicosea.model.dto.LoginRequestDTO;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.mapper.UserMapper;
import com.isabela.testetecnicosea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public User register(CreateUserClientRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        String hash = passwordEncoder.encode(request.password());
        User user = userMapper.toEntity(request, hash);
        return userRepository.save(user);
    }

    public String login(LoginRequestDTO request) {
        UserDetails userDetails = userRepository.findByEmail(request.email());

        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        User user = (User) userDetails;
        return tokenService.generateToken(user);
    }

}
