package com.isabela.testetecnicosea.service;

import com.isabela.testetecnicosea.model.dto.AnalystDecisionRequestDTO;
import com.isabela.testetecnicosea.model.entity.AnalystCoverageState;
import com.isabela.testetecnicosea.model.entity.Solicitation;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.AnalystDecision;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import com.isabela.testetecnicosea.model.enums.State;
import com.isabela.testetecnicosea.repository.AnalystCoverageStateRepository;
import com.isabela.testetecnicosea.repository.SolicitationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AnalystSolicitationServiceTest {

    @Mock
    private SolicitationRepository solicitationRepository;

    @Mock
    private AnalystCoverageStateRepository coverageRepository;

    @Mock
    private SolicitationIndexService solicitationIndexService;

    @InjectMocks
    private AnalystSolicitationService analystSolicitationService;


    // ---------- FIND BY ID ----------
    @Test
    void shouldFindSolicitationWhenAnalystHasCoverage() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.DF, SolicitationStatus.SUBMITTED);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));

        Solicitation result = analystSolicitationService.findById(10, analyst);

        assertEquals(solicitation, result);

        verify(solicitationRepository).findById(10);
        verify(coverageRepository).findByUserId(1);
    }

    @Test
    void shouldThrowNotFoundWhenSolicitationDoesNotExist() {
        User analyst = createAnalyst(1);

        when(solicitationRepository.findById(10)).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> analystSolicitationService.findById(10, analyst)
                );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(solicitationRepository).findById(10);
        verifyNoInteractions(coverageRepository);
    }


    @Test
    void shouldNotFindSolicitationWhenAnalystDoesNotHaveCoverage() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.SP, SolicitationStatus.SUBMITTED);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> analystSolicitationService.findById(10, analyst)
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }


    @Test
    void shouldFindSolicitationWhenAnalystHasOneOfMultipleCoverages() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.SP, SolicitationStatus.SUBMITTED);
        AnalystCoverageState dfCoverage = createCoverage(1, State.DF);
        AnalystCoverageState spCoverage = createCoverage(1, State.SP);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(dfCoverage, spCoverage));

        Solicitation result = analystSolicitationService.findById(10, analyst);

        assertEquals(solicitation, result);
    }


    // ---------- START ----------
    @Test
    void shouldStartSolicitationSuccessfully() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.DF, SolicitationStatus.SUBMITTED);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        Solicitation result = analystSolicitationService.start(10, analyst);

        assertEquals(SolicitationStatus.IN_REVIEW, result.getStatus());
        assertNotNull(result.getUpdatedAt());

        verify(solicitationRepository).save(solicitation);
        verify(solicitationIndexService).index(solicitation);
    }


    @Test
    void shouldNotStartSolicitationWhenStatusIsDraft() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.DF, SolicitationStatus.DRAFT);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> analystSolicitationService.start(10, analyst)
                );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("status SUBMITTED"));

        verify(solicitationRepository, never()).save(any());
        verifyNoInteractions(solicitationIndexService);
    }


    @Test
    void shouldNotStartSolicitationWhenStatusIsInReview() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.DF, SolicitationStatus.IN_REVIEW);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> analystSolicitationService.start(10, analyst)
                );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(solicitationRepository, never()).save(any());
        verifyNoInteractions(solicitationIndexService);
    }


    @Test
    void shouldNotStartSolicitationWithoutCoverage() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.SP, SolicitationStatus.SUBMITTED);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> analystSolicitationService.start(10, analyst)
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(solicitationRepository, never()).save(any());
        verifyNoInteractions(solicitationIndexService);
    }


    // ---------- DECIDE ----------
    @Test
    void shouldApproveSubmittedSolicitationSuccessfully() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.DF, SolicitationStatus.SUBMITTED);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        AnalystDecisionRequestDTO request =
                new AnalystDecisionRequestDTO(
                        AnalystDecision.APPROVE,
                        "Documentação analisada e solicitação aprovada."
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        Solicitation result = analystSolicitationService.decide(10, request, analyst);

        assertEquals(SolicitationStatus.APPROVED, result.getStatus());
        assertEquals("Documentação analisada e solicitação aprovada.",result.getAnalysisComment());
        assertEquals(analyst.getId(), result.getAnalyzedBy());
        assertNotNull(result.getAnalyzedAt());
        assertNotNull(result.getUpdatedAt());

        verify(solicitationRepository).save(solicitation);
        verify(solicitationIndexService).index(solicitation);
    }


    @Test
    void shouldRejectSubmittedSolicitationSuccessfully() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.DF, SolicitationStatus.SUBMITTED);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        AnalystDecisionRequestDTO request =
                new AnalystDecisionRequestDTO(
                        AnalystDecision.REJECT,
                        "Solicitação rejeitada devido aos dados apresentados."
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        Solicitation result = analystSolicitationService.decide(10, request, analyst);

        assertEquals(SolicitationStatus.REJECTED, result.getStatus());
        assertEquals("Solicitação rejeitada devido aos dados apresentados.",result.getAnalysisComment());
        assertEquals(analyst.getId(), result.getAnalyzedBy());
        assertNotNull(result.getAnalyzedAt());
        assertNotNull(result.getUpdatedAt());

        verify(solicitationRepository).save(solicitation);
        verify(solicitationIndexService).index(solicitation);
    }


    @Test
    void shouldApproveSolicitationWhenStatusIsInReview() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.DF, SolicitationStatus.IN_REVIEW);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        AnalystDecisionRequestDTO request =
                new AnalystDecisionRequestDTO(
                        AnalystDecision.APPROVE,
                        "Solicitação analisada e devidamente aprovada."
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        Solicitation result = analystSolicitationService.decide(10, request, analyst);

        assertEquals(SolicitationStatus.APPROVED, result.getStatus());

        verify(solicitationRepository).save(solicitation);
        verify(solicitationIndexService).index(solicitation);
    }


    @Test
    void shouldNotDecideDraftSolicitation() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.DF, SolicitationStatus.DRAFT);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        AnalystDecisionRequestDTO request =
                new AnalystDecisionRequestDTO(
                        AnalystDecision.APPROVE,
                        "Solicitação analisada e devidamente aprovada."
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> analystSolicitationService.decide(
                                10,
                                request,
                                analyst
                        )
                );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("SUBMITTED ou IN_REVIEW"));

        verify(solicitationRepository, never()).save(any());
        verifyNoInteractions(solicitationIndexService);
    }


    @Test
    void shouldNotDecideApprovedSolicitation() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.DF, SolicitationStatus.APPROVED);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        AnalystDecisionRequestDTO request =
                new AnalystDecisionRequestDTO(
                        AnalystDecision.REJECT,
                        "Tentativa de alterar uma decisão já finalizada."
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> analystSolicitationService.decide(
                                10,
                                request,
                                analyst
                        )
                );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(solicitationRepository, never()).save(any());
        verifyNoInteractions(solicitationIndexService);
    }


    @Test
    void shouldNotDecideRejectedSolicitation() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.DF, SolicitationStatus.REJECTED);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        AnalystDecisionRequestDTO request =
                new AnalystDecisionRequestDTO(
                        AnalystDecision.APPROVE,
                        "Tentativa de alterar uma decisão já finalizada."
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> analystSolicitationService.decide(
                                10,
                                request,
                                analyst
                        )
                );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(solicitationRepository, never()).save(any());
        verifyNoInteractions(solicitationIndexService);
    }


    @Test
    void shouldNotDecideSolicitationWithoutCoverage() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.SP, SolicitationStatus.SUBMITTED);
        AnalystCoverageState coverage = createCoverage(1, State.DF);

        AnalystDecisionRequestDTO request =
                new AnalystDecisionRequestDTO(
                        AnalystDecision.APPROVE,
                        "Solicitação analisada e devidamente aprovada."
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of(coverage));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> analystSolicitationService.decide(
                                10,
                                request,
                                analyst
                        )
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(solicitationRepository, never()).save(any());
        verifyNoInteractions(solicitationIndexService);
    }


    @Test
    void shouldNotAllowAnalystWithoutAnyCoverage() {
        User analyst = createAnalyst(1);
        Solicitation solicitation = createSolicitation(10, State.DF, SolicitationStatus.SUBMITTED);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.findByUserId(1)).thenReturn(List.of());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> analystSolicitationService.findById(10, analyst)
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }


    // ---------- MÉTODOS AUXILIARES ----------
    private User createAnalyst(Integer id) {
        User analyst = new User();
        analyst.setId(id);
        return analyst;
    }


    private Solicitation createSolicitation(
            Integer id,
            State state,
            SolicitationStatus status
    ) {
        Solicitation solicitation = new Solicitation();

        solicitation.setId(id);
        solicitation.setClientId(100);
        solicitation.setState(state);
        solicitation.setStatus(status);

        return solicitation;
    }


    private AnalystCoverageState createCoverage(
            Integer analystId,
            State state
    ) {
        AnalystCoverageState coverage =
                new AnalystCoverageState();

        coverage.setUserId(analystId);
        coverage.setState(state);
        coverage.setCreatedAt(LocalDateTime.now());

        return coverage;
    }


}
