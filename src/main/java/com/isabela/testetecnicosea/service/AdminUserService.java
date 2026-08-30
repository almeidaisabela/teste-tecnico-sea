package com.isabela.testetecnicosea.service;

import com.isabela.testetecnicosea.model.dto.CreateInternalUserRequestDTO;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.UserRole;
import com.isabela.testetecnicosea.model.mapper.UserMapper;
import com.isabela.testetecnicosea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createInternalUser(CreateInternalUserRequestDTO request) {
        if (request.role() == UserRole.CLIENT) {
            throw new IllegalArgumentException("Use /auth/register para criar usuários CLIENT.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado no sistema.");
        }

        String hash = passwordEncoder.encode(request.password());
        User user = userMapper.toEntity(request, hash);
        return userRepository.save(user);
    }

}
