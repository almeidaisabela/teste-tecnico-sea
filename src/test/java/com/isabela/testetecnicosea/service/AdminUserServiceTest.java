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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AnalystCoverageStateRepository analystCoverageStateRepository;

    @InjectMocks
    private AdminUserService adminUserService;


    // ---------- CREATE INTERNAL USER ----------
    @Test
    void shouldCreateAnalystSuccessfully() {
        CreateInternalUserRequestDTO request =
                new CreateInternalUserRequestDTO(
                        "Maria Silva",
                        "maria@email.com",
                        "senha123",
                        UserRole.ANALYST
                );

        User mappedUser = createUser(
                null,
                "Maria Silva",
                "maria@email.com",
                UserRole.ANALYST
        );

        User savedUser = createUser(
                10,
                "Maria Silva",
                "maria@email.com",
                UserRole.ANALYST
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("senha-hash");
        when(userMapper.toEntity(request, "senha-hash")).thenReturn(mappedUser);
        when(userRepository.save(mappedUser)).thenReturn(savedUser);

        User result = adminUserService.createInternalUser(request);

        assertEquals(savedUser, result);

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userMapper).toEntity(request, "senha-hash");
        verify(userRepository).save(mappedUser);
    }


    @Test
    void shouldCreateAdminSuccessfully() {
        CreateInternalUserRequestDTO request =
                new CreateInternalUserRequestDTO(
                        "Administrador",
                        "admin@email.com",
                        "senha123",
                        UserRole.ADMIN
                );

        User mappedUser = createUser(
                null,
                "Administrador",
                "admin@email.com",
                UserRole.ADMIN
        );

        User savedUser = createUser(
                20,
                "Administrador",
                "admin@email.com",
                UserRole.ADMIN
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("senha-hash");
        when(userMapper.toEntity(request, "senha-hash")).thenReturn(mappedUser);
        when(userRepository.save(mappedUser)).thenReturn(savedUser);

        User result = adminUserService.createInternalUser(request);

        assertEquals(savedUser, result);
        assertEquals(UserRole.ADMIN, result.getRole());

        verify(userRepository).save(mappedUser);
    }


    @Test
    void shouldNotCreateClientAsInternalUser() {
        CreateInternalUserRequestDTO request =
                new CreateInternalUserRequestDTO(
                        "Cliente",
                        "cliente@email.com",
                        "senha123",
                        UserRole.CLIENT
                );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> adminUserService.createInternalUser(request)
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(userMapper);
        verify(userRepository, never()).save(any());
    }


    @Test
    void shouldNotCreateInternalUserWhenEmailAlreadyExists() {
        CreateInternalUserRequestDTO request =
                new CreateInternalUserRequestDTO(
                        "Maria Silva",
                        "maria@email.com",
                        "senha123",
                        UserRole.ANALYST
                );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> adminUserService.createInternalUser(request)
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


    // ---------- UPDATE COVERAGE ----------
    @Test
    void shouldUpdateAnalystCoverageSuccessfully() {
        Integer analystId = 10;

        User analyst =
                createUser(
                        analystId,
                        "Analista",
                        "analista@email.com",
                        UserRole.ANALYST
                );

        AnalystCoverageRequestDTO request =
                new AnalystCoverageRequestDTO(
                        List.of(State.DF, State.SP)
                );

        AnalystCoverageState existingCoverage =
                createCoverage(
                        analystId,
                        State.RJ
                );

        when(userRepository.findById(analystId)).thenReturn(Optional.of(analyst));
        when(analystCoverageStateRepository.findByUserId(analystId)).thenReturn(List.of(existingCoverage));

        AnalystCoverageResponseDTO result =
                adminUserService.updateCoverage(
                        analystId,
                        request
                );

        assertEquals(analystId, result.userId());
        assertEquals(
                List.of(State.DF, State.SP),
                result.states()
        );

        verify(analystCoverageStateRepository).deleteAll(List.of(existingCoverage));
        verify(analystCoverageStateRepository).saveAll(anyList());
    }


    @Test
    void shouldRemoveDuplicateStatesWhenUpdatingCoverage() {
        Integer analystId = 10;

        User analyst =
                createUser(
                        analystId,
                        "Analista",
                        "analista@email.com",
                        UserRole.ANALYST
                );

        AnalystCoverageRequestDTO request =
                new AnalystCoverageRequestDTO(
                        List.of(
                                State.DF,
                                State.SP,
                                State.DF,
                                State.SP
                        )
                );

        when(userRepository.findById(analystId)).thenReturn(Optional.of(analyst));
        when(analystCoverageStateRepository.findByUserId(analystId)).thenReturn(List.of());

        AnalystCoverageResponseDTO result =
                adminUserService.updateCoverage(
                        analystId,
                        request
                );

        assertEquals(2, result.states().size());

        assertEquals(
                List.of(State.DF, State.SP),
                result.states()
        );
    }


    @Test
    void shouldSaveCorrectCoverageStates() {
        Integer analystId = 10;

        User analyst =
                createUser(
                        analystId,
                        "Analista",
                        "analista@email.com",
                        UserRole.ANALYST
                );

        AnalystCoverageRequestDTO request =
                new AnalystCoverageRequestDTO(
                        List.of(State.DF, State.SP)
                );

        when(userRepository.findById(analystId)).thenReturn(Optional.of(analyst));
        when(analystCoverageStateRepository.findByUserId(analystId)).thenReturn(List.of());

        adminUserService.updateCoverage(
                analystId,
                request
        );

        ArgumentCaptor<List<AnalystCoverageState>> captor = ArgumentCaptor.forClass(List.class);

        verify(analystCoverageStateRepository).saveAll(captor.capture());

        List<AnalystCoverageState> savedCoverages = captor.getValue();

        assertEquals(2, savedCoverages.size());

        assertEquals(
                analystId,
                savedCoverages.get(0).getUserId()
        );

        assertEquals(
                State.DF,
                savedCoverages.get(0).getState()
        );

        assertNotNull(
                savedCoverages.get(0).getCreatedAt()
        );

        assertEquals(
                analystId,
                savedCoverages.get(1).getUserId()
        );

        assertEquals(
                State.SP,
                savedCoverages.get(1).getState()
        );

        assertNotNull(savedCoverages.get(1).getCreatedAt());
    }


    @Test
    void shouldDeleteOldCoverageBeforeSavingNewCoverage() {
        Integer analystId = 10;

        User analyst =
                createUser(
                        analystId,
                        "Analista",
                        "analista@email.com",
                        UserRole.ANALYST
                );

        AnalystCoverageRequestDTO request =
                new AnalystCoverageRequestDTO(
                        List.of(State.SP)
                );

        AnalystCoverageState oldCoverage1 =
                createCoverage(
                        analystId,
                        State.DF
                );

        AnalystCoverageState oldCoverage2 =
                createCoverage(
                        analystId,
                        State.RJ
                );

        List<AnalystCoverageState> oldCoverages =
                List.of(
                        oldCoverage1,
                        oldCoverage2
                );

        when(userRepository.findById(analystId)).thenReturn(Optional.of(analyst));
        when(analystCoverageStateRepository.findByUserId(analystId)).thenReturn(oldCoverages);

        adminUserService.updateCoverage(
                analystId,
                request
        );

        verify(analystCoverageStateRepository).deleteAll(oldCoverages);
        verify(analystCoverageStateRepository).saveAll(anyList());
    }


    @Test
    void shouldNotUpdateCoverageWhenUserDoesNotExist() {
        Integer analystId = 10;

        AnalystCoverageRequestDTO request =
                new AnalystCoverageRequestDTO(
                        List.of(State.DF)
                );

        when(userRepository.findById(analystId)).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> adminUserService.updateCoverage(
                                analystId,
                                request
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verifyNoInteractions(analystCoverageStateRepository);
    }


    @Test
    void shouldNotUpdateCoverageForAdminUser() {
        Integer userId = 10;

        User admin =
                createUser(
                        userId,
                        "Admin",
                        "admin@email.com",
                        UserRole.ADMIN
                );

        AnalystCoverageRequestDTO request =
                new AnalystCoverageRequestDTO(
                        List.of(State.DF)
                );

        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> adminUserService.updateCoverage(
                                userId,
                                request
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(analystCoverageStateRepository);
    }


    @Test
    void shouldNotUpdateCoverageForClientUser() {
        Integer userId = 10;

        User client =
                createUser(
                        userId,
                        "Cliente",
                        "cliente@email.com",
                        UserRole.CLIENT
                );

        AnalystCoverageRequestDTO request =
                new AnalystCoverageRequestDTO(
                        List.of(State.DF)
                );

        when(userRepository.findById(userId)).thenReturn(Optional.of(client));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> adminUserService.updateCoverage(
                                userId,
                                request
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(analystCoverageStateRepository);
    }


    // ---------- GET COVERAGE ----------
    @Test
    void shouldGetAnalystCoverageSuccessfully() {
        Integer analystId = 10;

        User analyst =
                createUser(
                        analystId,
                        "Analista",
                        "analista@email.com",
                        UserRole.ANALYST
                );

        AnalystCoverageState dfCoverage =
                createCoverage(
                        analystId,
                        State.DF
                );

        AnalystCoverageState spCoverage =
                createCoverage(
                        analystId,
                        State.SP
                );

        when(userRepository.findById(analystId)).thenReturn(Optional.of(analyst));

        when(analystCoverageStateRepository.findByUserId(analystId))
                .thenReturn(
                        List.of(
                                dfCoverage,
                                spCoverage
                        )
                );

        AnalystCoverageResponseDTO result =
                adminUserService.getCoverage(
                        analystId
                );

        assertEquals(analystId, result.userId());

        assertEquals(
                List.of(State.DF, State.SP),
                result.states()
        );

        verify(userRepository).findById(analystId);
        verify(analystCoverageStateRepository).findByUserId(analystId);
    }


    @Test
    void shouldReturnEmptyCoverageWhenAnalystHasNoStates() {
        Integer analystId = 10;

        User analyst =
                createUser(
                        analystId,
                        "Analista",
                        "analista@email.com",
                        UserRole.ANALYST
                );

        when(userRepository.findById(analystId)).thenReturn(Optional.of(analyst));
        when(analystCoverageStateRepository.findByUserId(analystId)).thenReturn(List.of());

        AnalystCoverageResponseDTO result =
                adminUserService.getCoverage(
                        analystId
                );

        assertTrue(result.states().isEmpty());
    }


    @Test
    void shouldNotGetCoverageWhenUserDoesNotExist() {
        Integer analystId = 10;

        when(userRepository.findById(analystId)).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> adminUserService.getCoverage(
                                analystId
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(
                analystCoverageStateRepository,
                never()
        ).findByUserId(anyInt());
    }


    // ---------- MÉTODOS AUXILIARES ----------
    private User createUser(
            Integer id,
            String name,
            String email,
            UserRole role
    ) {
        User user = new User();

        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled(true);

        return user;
    }


    private AnalystCoverageState createCoverage(
            Integer analystId,
            State state
    ) {
        AnalystCoverageState coverage =
                new AnalystCoverageState();

        coverage.setUserId(analystId);
        coverage.setState(state);

        return coverage;
    }

}
