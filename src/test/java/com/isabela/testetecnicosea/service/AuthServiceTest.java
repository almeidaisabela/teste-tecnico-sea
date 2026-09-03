package com.isabela.testetecnicosea.service;

import com.isabela.testetecnicosea.model.dto.CreateUserClientRequestDTO;
import com.isabela.testetecnicosea.model.dto.LoginRequestDTO;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.UserRole;
import com.isabela.testetecnicosea.model.mapper.UserMapper;
import com.isabela.testetecnicosea.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;


    // ---------- REGISTER ----------
    @Test
    void shouldRegisterClientSuccessfully() {
        CreateUserClientRequestDTO request =
                new CreateUserClientRequestDTO(
                        "Maria Silva",
                        "maria@email.com",
                        "senha123"
                );

        User mappedUser = createClient(
                null,
                "Maria Silva",
                "maria@email.com"
        );

        User savedUser = createClient(
                10,
                "Maria Silva",
                "maria@email.com"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("senha-hash");
        when(userMapper.toEntity(request, "senha-hash")).thenReturn(mappedUser);
        when(userRepository.save(mappedUser)).thenReturn(savedUser);

        User result = authService.register(request);

        assertEquals(savedUser, result);
        assertEquals(10, result.getId());
        assertEquals(UserRole.CLIENT, result.getRole());

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userMapper).toEntity(request, "senha-hash");
        verify(userRepository).save(mappedUser);
    }


    @Test
    void shouldNotRegisterWhenEmailAlreadyExists() {
        CreateUserClientRequestDTO request =
                new CreateUserClientRequestDTO(
                        "Maria Silva",
                        "maria@email.com",
                        "senha123"
                );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(userRepository).existsByEmail(request.email());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(userMapper);
        verify(userRepository, never()).save(any());
    }


    @Test
    void shouldEncodePasswordBeforeRegisteringClient() {
        CreateUserClientRequestDTO request =
                new CreateUserClientRequestDTO(
                        "Maria Silva",
                        "maria@email.com",
                        "senha123"
                );

        User mappedUser = createClient(
                null,
                "Maria Silva",
                "maria@email.com"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-gerado");
        when(userMapper.toEntity(request, "hash-gerado")).thenReturn(mappedUser);
        when(userRepository.save(mappedUser)).thenReturn(mappedUser);

        authService.register(request);

        verify(passwordEncoder).encode("senha123");
        verify(userMapper).toEntity(request, "hash-gerado");
    }


    // ---------- LOGIN ----------
    @Test
    void shouldLoginSuccessfully() {
        LoginRequestDTO request =
                new LoginRequestDTO(
                        "maria@email.com",
                        "senha123"
                );

        User user = createClient(
                10,
                "Maria Silva",
                "maria@email.com"
        );

        user.setPasswordHash("senha-hash");

        when(userRepository.findByEmail(request.email())).thenReturn(user);

        when(
                passwordEncoder.matches(
                        request.password(),
                        user.getPassword()
                )
        ).thenReturn(true);

        when(tokenService.generateToken(user)).thenReturn("jwt-token");

        String result = authService.login(request);

        assertEquals("jwt-token", result);

        verify(userRepository).findByEmail(request.email());

        verify(passwordEncoder)
                .matches(
                        request.password(),
                        user.getPassword()
                );

        verify(tokenService).generateToken(user);
    }


    @Test
    void shouldNotLoginWhenUserDoesNotExist() {
        LoginRequestDTO request =
                new LoginRequestDTO(
                        "inexistente@email.com",
                        "senha123"
                );

        when(userRepository.findByEmail(request.email())).thenReturn(null);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verify(userRepository).findByEmail(request.email());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(tokenService);
    }


    @Test
    void shouldNotLoginWhenPasswordIsIncorrect() {
        LoginRequestDTO request =
                new LoginRequestDTO(
                        "maria@email.com",
                        "senhaErrada"
                );

        User user = createClient(
                10,
                "Maria Silva",
                "maria@email.com"
        );

        user.setPasswordHash("senha-hash-correto");

        when(userRepository.findByEmail(request.email())).thenReturn(user);

        when(
                passwordEncoder.matches(
                        request.password(),
                        user.getPassword()
                )
        ).thenReturn(false);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verify(passwordEncoder)
                .matches(
                        request.password(),
                        user.getPassword()
                );

        verifyNoInteractions(tokenService);
    }


    @Test
    void shouldGenerateTokenOnlyWhenPasswordMatches() {
        LoginRequestDTO request =
                new LoginRequestDTO(
                        "maria@email.com",
                        "senha123"
                );

        User user = createClient(
                10,
                "Maria Silva",
                "maria@email.com"
        );

        user.setPasswordHash("senha-hash");

        when(userRepository.findByEmail(request.email())).thenReturn(user);

        when(
                passwordEncoder.matches(
                        request.password(),
                        user.getPassword()
                )
        ).thenReturn(true);

        when(tokenService.generateToken(user)).thenReturn("token-gerado");

        authService.login(request);

        verify(tokenService).generateToken(user);
    }


    // ---------- MÉTODOS AUXILIARES ----------
    private User createClient(
            Integer id,
            String name,
            String email
    ) {
        User user = new User();

        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setRole(UserRole.CLIENT);
        user.setEnabled(true);

        return user;
    }
}
