package com.isabela.testetecnicosea.service;

import com.isabela.testetecnicosea.model.dto.SolicitationStep1RequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep2RequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep3RequestDTO;
import com.isabela.testetecnicosea.model.dto.ViaCepResponseDTO;
import com.isabela.testetecnicosea.model.entity.Solicitation;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.Priority;
import com.isabela.testetecnicosea.model.enums.ServiceType;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import com.isabela.testetecnicosea.model.enums.State;
import com.isabela.testetecnicosea.model.mapper.SolicitationMapper;
import com.isabela.testetecnicosea.repository.SolicitationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SolicitationServiceTest {

    @Mock
    private SolicitationRepository solicitationRepository;

    @Mock
    private SolicitationMapper solicitationMapper;

    @Mock
    private CepService cepService;

    @Mock
    private SolicitationIndexService solicitationIndexService;

    @InjectMocks
    private SolicitationService solicitationService;


    // ---------- CREATE ----------
    @Test
    void shouldCreateSolicitationSuccessfully() {
        User client = createClient(1);

        Solicitation solicitation = new Solicitation();
        solicitation.setClientId(client.getId());
        solicitation.setStatus(SolicitationStatus.DRAFT);
        solicitation.setCurrentStep(0);

        Solicitation savedSolicitation = new Solicitation();
        savedSolicitation.setId(10);
        savedSolicitation.setClientId(client.getId());
        savedSolicitation.setStatus(SolicitationStatus.DRAFT);
        savedSolicitation.setCurrentStep(0);

        when(solicitationMapper.toNewEntity(client.getId())).thenReturn(solicitation);
        when(solicitationRepository.save(solicitation)).thenReturn(savedSolicitation);

        Solicitation result = solicitationService.create(client);

        assertEquals(savedSolicitation, result);

        verify(solicitationMapper).toNewEntity(client.getId());
        verify(solicitationRepository).save(solicitation);
        verify(solicitationIndexService).index(savedSolicitation);
    }


    // ---------- STEP 1 ----------
    @Test
    void shouldSaveStep1Successfully() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 1, 0);

        SolicitationStep1RequestDTO request =
                new SolicitationStep1RequestDTO(
                        ServiceType.INSTALLATION,
                        "Instalação de ar condicionado",
                        "Preciso instalar um ar condicionado na sala da minha residência."
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        Solicitation result = solicitationService.saveStep1(10, request, client);

        assertEquals(1, result.getCurrentStep());
        assertNotNull(result.getUpdatedAt());

        verify(solicitationMapper).updateStep1(request, solicitation);
        verify(solicitationRepository).save(solicitation);
        verify(solicitationIndexService).index(solicitation);
    }


    @Test
    void shouldNotSaveStep1WhenSolicitationDoesNotExist() {
        User client = createClient(1);

        SolicitationStep1RequestDTO request =
                new SolicitationStep1RequestDTO(
                        ServiceType.INSTALLATION,
                        "Instalação",
                        "Descrição válida com mais de vinte caracteres."
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.saveStep1(10, request, client)
                );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(solicitationRepository, never()).save(any());
        verifyNoInteractions(solicitationIndexService);
    }


    @Test
    void shouldNotSaveStep1WhenSolicitationBelongsToAnotherClient() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 2, 0);

        SolicitationStep1RequestDTO request =
                new SolicitationStep1RequestDTO(
                        ServiceType.INSTALLATION,
                        "Instalação",
                        "Descrição válida com mais de vinte caracteres."
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.saveStep1(10, request, client)
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(solicitationRepository, never()).save(any());
        verifyNoInteractions(solicitationIndexService);
    }


    @Test
    void shouldNotSaveStep1WhenSolicitationIsNotDraft() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 1, 0);
        solicitation.setStatus(SolicitationStatus.SUBMITTED);

        SolicitationStep1RequestDTO request =
                new SolicitationStep1RequestDTO(
                        ServiceType.INSTALLATION,
                        "Instalação",
                        "Descrição válida com mais de vinte caracteres."
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.saveStep1(10, request, client)
                );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(solicitationRepository, never()).save(any());
    }


    // ---------- STEP 2 ----------
    @Test
    void shouldSaveStep2Successfully() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 1, 1);

        SolicitationStep2RequestDTO request =
                new SolicitationStep2RequestDTO(
                        "01310-100",
                        "1578",
                        "Apto 42"
                );

        ViaCepResponseDTO viaCepResponse =
                new ViaCepResponseDTO(
                        "Avenida Paulista",
                        "Bela Vista",
                        "São Paulo",
                        "SP",
                        false
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(cepService.findAdress(request.cep())).thenReturn(viaCepResponse);
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        Solicitation result = solicitationService.saveStep2(10, request, client);

        assertEquals(2, result.getCurrentStep());
        assertEquals("Avenida Paulista", result.getStreet());
        assertEquals("Bela Vista", result.getNeighborhood());
        assertEquals("São Paulo", result.getCity());
        assertEquals(State.SP, result.getState());
        assertNotNull(result.getUpdatedAt());

        verify(solicitationMapper).updateStep2(request, solicitation);
        verify(cepService).findAdress(request.cep());
        verify(solicitationRepository).save(solicitation);
        verify(solicitationIndexService).index(solicitation);
    }


    @Test
    void shouldNotSaveStep2WhenStep1IsNotCompleted() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 1, 0);

        SolicitationStep2RequestDTO request =
                new SolicitationStep2RequestDTO(
                        "01310-100",
                        "1578",
                        null
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.saveStep2(10, request, client)
                );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(cepService);
        verify(solicitationRepository, never()).save(any());
    }


    // ---------- STEP 3 ----------
    @Test
    void shouldSaveStep3Successfully() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 1, 2);

        SolicitationStep3RequestDTO request =
                new SolicitationStep3RequestDTO(
                        Priority.MEDIUM,
                        LocalDate.now().plusDays(5),
                        BigDecimal.valueOf(250),
                        true
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        Solicitation result = solicitationService.saveStep3(10, request, client);

        assertEquals(3, result.getCurrentStep());
        assertNotNull(result.getUpdatedAt());

        verify(solicitationMapper).updateStep3(request, solicitation);
        verify(solicitationRepository).save(solicitation);
        verify(solicitationIndexService).index(solicitation);
    }


    @Test
    void shouldNotSaveStep3WhenStep2IsNotCompleted() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 1, 1);

        SolicitationStep3RequestDTO request =
                new SolicitationStep3RequestDTO(
                        Priority.MEDIUM,
                        LocalDate.now().plusDays(5),
                        BigDecimal.valueOf(250),
                        true
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.saveStep3(10, request, client)
                );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(solicitationRepository, never()).save(any());
    }


    @Test
    void shouldNotSaveHighPriorityWhenEstimatedValueIsBelow100() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 1, 2);

        SolicitationStep3RequestDTO request =
                new SolicitationStep3RequestDTO(
                        Priority.HIGH,
                        LocalDate.now().plusDays(5),
                        BigDecimal.valueOf(99),
                        true
                );

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.saveStep3(10, request, client)
                );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Valor estimado deve ser >= 100"));

        verify(solicitationMapper, never()).updateStep3(any(), any());
        verify(solicitationRepository, never()).save(any());
    }


    // ---------- SUBMIT ----------
    @Test
    void shouldSubmitSolicitationSuccessfully() {
        User client = createClient(1);
        Solicitation solicitation = createCompleteSolicitation(10, 1);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        Solicitation result = solicitationService.submit(10, client);

        assertEquals(SolicitationStatus.SUBMITTED, result.getStatus());
        assertNotNull(result.getSubmittedAt());
        assertNotNull(result.getUpdatedAt());

        verify(solicitationRepository).save(solicitation);
        verify(solicitationIndexService).index(solicitation);
    }


    @Test
    void shouldNotSubmitIncompleteSolicitation() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 1, 3);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.submit(10, client)
                );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Solicitação incompleta"));

        verify(solicitationRepository, never()).save(any());
        verifyNoInteractions(solicitationIndexService);
    }


    @Test
    void shouldNotSubmitWhenPreferredDateIsInThePast() {
        User client = createClient(1);
        Solicitation solicitation = createCompleteSolicitation(10, 1);
        solicitation.setPreferredDate(LocalDate.now().minusDays(1));

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.submit(10, client)
                );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("PreferredDate está no passado"));

        verify(solicitationRepository, never()).save(any());
    }


    @Test
    void shouldNotSubmitWhenTermsAreNotAccepted() {
        User client = createClient(1);
        Solicitation solicitation = createCompleteSolicitation(10, 1);
        solicitation.setTermsAccepted(false);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.submit(10, client)
                );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("TermsAccepted deve ser true"));

        verify(solicitationRepository, never()).save(any());
    }


    @Test
    void shouldNotSubmitHighPriorityWhenEstimatedValueIsBelow100() {
        User client = createClient(1);
        Solicitation solicitation = createCompleteSolicitation(10, 1);
        solicitation.setPriority(Priority.HIGH);
        solicitation.setEstimatedValue(BigDecimal.valueOf(50));

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.submit(10, client)
                );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("EstimatedValue deve ser >= 100"));

        verify(solicitationRepository, never()).save(any());
    }


    @Test
    void shouldNotSubmitSolicitationFromAnotherClient() {
        User client = createClient(1);
        Solicitation solicitation = createCompleteSolicitation(10, 2);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.submit(10, client)
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(solicitationRepository, never()).save(any());
    }


    @Test
    void shouldNotSubmitSolicitationWhenStatusIsNotDraft() {
        User client = createClient(1);
        Solicitation solicitation = createCompleteSolicitation(10, 1);
        solicitation.setStatus(SolicitationStatus.SUBMITTED);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.submit(10, client)
                );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(solicitationRepository, never()).save(any());
    }


    // ---------- FIND BY ID ----------
    @Test
    void shouldFindSolicitationByIdSuccessfully() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 1, 1);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        Solicitation result = solicitationService.findById(10, client);

        assertEquals(solicitation, result);
    }


    @Test
    void shouldThrowNotFoundWhenFindingNonExistingSolicitation() {
        User client = createClient(1);

        when(solicitationRepository.findById(10)).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.findById(10, client)
                );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }


    @Test
    void shouldNotFindSolicitationFromAnotherClient() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 2, 1);

        when(solicitationRepository.findById(10)).thenReturn(Optional.of(solicitation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> solicitationService.findById(10, client)
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }


    // ---------- LIST ----------
    @Test
    void shouldListAllSolicitationsFromClient() {
        User client = createClient(1);
        Solicitation solicitation1 = createDraftSolicitation(10, 1, 1);
        Solicitation solicitation2 = createDraftSolicitation(11, 1, 2);

        List<Solicitation> solicitations = List.of(solicitation1, solicitation2);

        when(solicitationRepository.findByClientId(1)).thenReturn(solicitations);

        List<Solicitation> result = solicitationService.list(client, null);

        assertEquals(2, result.size());
        assertEquals(solicitations, result);

        verify(solicitationRepository).findByClientId(1);
        verify(solicitationRepository, never()).findByClientIdAndStatus(anyInt(), any());
    }


    @Test
    void shouldListSolicitationsFilteredByStatus() {
        User client = createClient(1);
        Solicitation solicitation = createDraftSolicitation(10, 1, 3);
        solicitation.setStatus(SolicitationStatus.SUBMITTED);
        List<Solicitation> solicitations = List.of(solicitation);

        when(
                solicitationRepository.findByClientIdAndStatus(
                        1,
                        SolicitationStatus.SUBMITTED
                )
        ).thenReturn(solicitations);

        List<Solicitation> result =
                solicitationService.list(
                        client,
                        SolicitationStatus.SUBMITTED
                );

        assertEquals(1, result.size());
        assertEquals(SolicitationStatus.SUBMITTED, result.getFirst().getStatus());

        verify(solicitationRepository)
                .findByClientIdAndStatus(
                        1,
                        SolicitationStatus.SUBMITTED
                );

        verify(solicitationRepository, never()).findByClientId(anyInt());
    }


    // ---------- MÉTODOS AUXILIARES ----------
    private User createClient(Integer id) {
        User client = new User();
        client.setId(id);
        return client;
    }


    private Solicitation createDraftSolicitation(
            Integer id,
            Integer clientId,
            Integer currentStep
    ) {
        Solicitation solicitation = new Solicitation();

        solicitation.setId(id);
        solicitation.setClientId(clientId);
        solicitation.setStatus(SolicitationStatus.DRAFT);
        solicitation.setCurrentStep(currentStep);

        return solicitation;
    }


    private Solicitation createCompleteSolicitation(
            Integer id,
            Integer clientId
    ) {
        Solicitation solicitation =createDraftSolicitation(id, clientId, 3);

        solicitation.setServiceType(ServiceType.INSTALLATION);
        solicitation.setTitle("Instalação de ar condicionado");
        solicitation.setDescription(
                "Preciso instalar um ar condicionado na sala da minha residência."
        );

        solicitation.setCep("01310-100");
        solicitation.setNumber("1578");
        solicitation.setStreet("Avenida Paulista");
        solicitation.setNeighborhood("Bela Vista");
        solicitation.setCity("São Paulo");
        solicitation.setState(State.SP);

        solicitation.setPriority(Priority.MEDIUM);
        solicitation.setPreferredDate(LocalDate.now().plusDays(5));
        solicitation.setEstimatedValue(BigDecimal.valueOf(250));
        solicitation.setTermsAccepted(true);

        return solicitation;
    }


}