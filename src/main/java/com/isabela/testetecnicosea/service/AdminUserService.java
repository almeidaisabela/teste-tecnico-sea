package com.isabela.testetecnicosea.service;

import com.isabela.testetecnicosea.model.dto.AnalystCoverageRequestDTO;
import com.isabela.testetecnicosea.model.dto.AnalystCoverageResponseDTO;
import com.isabela.testetecnicosea.model.dto.CreateInternalUserRequestDTO;
import com.isabela.testetecnicosea.model.entity.AnalystCoverageState;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.State;
import com.isabela.testetecnicosea.model.enums.UserRole;
import com.isabela.testetecnicosea.model.mapper.UserMapper;
import com.isabela.testetecnicosea.repository.AnalystCoverageStateRepository;
import com.isabela.testetecnicosea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnalystCoverageStateRepository analystCoverageStateRepository;

    public User createInternalUser(CreateInternalUserRequestDTO request) {
        if (request.role() == UserRole.CLIENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use /auth/register para criar usuários CLIENT");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado no sistema");
        }

        String hash = passwordEncoder.encode(request.password());
        User user = userMapper.toEntity(request, hash);
        return userRepository.save(user);
    }


    public AnalystCoverageResponseDTO updateCoverage(Integer analystId, AnalystCoverageRequestDTO request) {
        User analyst = userRepository.findById(analystId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (analyst.getRole() != UserRole.ANALYST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas usuários ANALYST podem ter cobertura de UF");
        }

        List<AnalystCoverageState> existentes = analystCoverageStateRepository.findByUserId(analystId);
        analystCoverageStateRepository.deleteAll(existentes);
        List<State> uniqueStates = request.states().stream().distinct().toList();

        List<AnalystCoverageState> novos = uniqueStates.stream()
                .map(state -> {
                    AnalystCoverageState coverage = new AnalystCoverageState();
                    coverage.setUserId(analystId);
                    coverage.setState(state);
                    coverage.setCreatedAt(LocalDateTime.now());
                    return coverage;
                })
                .toList();

        analystCoverageStateRepository.saveAll(novos);
        return new AnalystCoverageResponseDTO(analystId, uniqueStates);
    }


    public AnalystCoverageResponseDTO getCoverage(Integer analystId) {
        userRepository.findById(analystId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        List<State> states = analystCoverageStateRepository.findByUserId(analystId)
                .stream()
                .map(AnalystCoverageState::getState)
                .toList();

        return new AnalystCoverageResponseDTO(analystId, states);
    }

}
