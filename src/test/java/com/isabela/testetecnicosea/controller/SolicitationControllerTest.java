package com.isabela.testetecnicosea.controller;

import com.isabela.testetecnicosea.model.dto.SolicitationResponseDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep1RequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep2RequestDTO;
import com.isabela.testetecnicosea.model.dto.SolicitationStep3RequestDTO;
import com.isabela.testetecnicosea.model.entity.Solicitation;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.Priority;
import com.isabela.testetecnicosea.model.enums.ServiceType;
import com.isabela.testetecnicosea.model.enums.SolicitationStatus;
import com.isabela.testetecnicosea.model.enums.State;
import com.isabela.testetecnicosea.model.mapper.SolicitationMapper;
import com.isabela.testetecnicosea.service.SolicitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SolicitationControllerTest {

    @Mock
    private SolicitationService solicitationService;

    @Mock
    private SolicitationMapper solicitationMapper;

    @InjectMocks
    private SolicitationController solicitationController;

    private MockMvc mockMvc;

    private User client;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(solicitationController)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver()
                )
                .build();

        client = new User();
        client.setId(1);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        client,
                        null,
                        client.getAuthorities()
                )
        );
    }


    // ---------- CREATE ----------
    @Test
    void shouldCreateSolicitationSuccessfully() throws Exception {
        Solicitation solicitation = createSolicitation();

        SolicitationResponseDTO response =
                createResponseDTO(
                        SolicitationStatus.DRAFT,
                        0
                );

        when(solicitationService.create(client)).thenReturn(solicitation);
        when(solicitationMapper.toResponse(solicitation)).thenReturn(response);

        mockMvc.perform(
                        post("/solicitations")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.currentStep").value(0));

        verify(solicitationService).create(client);
        verify(solicitationMapper).toResponse(solicitation);
    }


    // ---------- STEP 1 ----------
    @Test
    void shouldUpdateStep1Successfully() throws Exception {
        Solicitation solicitation = createSolicitation();
        solicitation.setCurrentStep(1);

        SolicitationResponseDTO response =
                createResponseDTO(
                        SolicitationStatus.DRAFT,
                        1
                );

        when(
                solicitationService.saveStep1(
                        eq(10),
                        any(SolicitationStep1RequestDTO.class),
                        eq(client)
                )
        ).thenReturn(solicitation);

        when(solicitationMapper.toResponse(solicitation)).thenReturn(response);

        String json = """
                {
                    "serviceType": "INSTALLATION",
                    "title": "Instalação de ar condicionado",
                    "description": "Preciso instalar um ar condicionado na sala da minha residência."
                }
                """;

        mockMvc.perform(
                        put("/solicitations/10/step1")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.currentStep").value(1));

        verify(solicitationService)
                .saveStep1(
                        eq(10),
                        any(SolicitationStep1RequestDTO.class),
                        eq(client)
                );
    }


    @Test
    void shouldReturnBadRequestWhenStep1TitleIsTooShort() throws Exception {
        String json = """
                {
                    "serviceType": "INSTALLATION",
                    "title": "AB",
                    "description": "Descrição válida com mais de vinte caracteres."
                }
                """;

        mockMvc.perform(
                        put("/solicitations/10/step1")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(solicitationService);
    }


    @Test
    void shouldReturnBadRequestWhenStep1DescriptionIsTooShort() throws Exception {
        String json = """
                {
                    "serviceType": "INSTALLATION",
                    "title": "Título válido",
                    "description": "Curta"
                }
                """;

        mockMvc.perform(
                        put("/solicitations/10/step1")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(solicitationService);
    }


    // ---------- STEP 2 ----------
    @Test
    void shouldUpdateStep2Successfully() throws Exception {
        Solicitation solicitation = createSolicitation();
        solicitation.setCurrentStep(2);

        SolicitationResponseDTO response =
                createResponseDTO(
                        SolicitationStatus.DRAFT,
                        2
                );

        when(
                solicitationService.saveStep2(
                        eq(10),
                        any(SolicitationStep2RequestDTO.class),
                        eq(client)
                )
        ).thenReturn(solicitation);

        when(solicitationMapper.toResponse(solicitation)).thenReturn(response);

        String json = """
                {
                    "cep": "01310-100",
                    "number": "1578",
                    "complement": "Apto 42"
                }
                """;

        mockMvc.perform(
                        put("/solicitations/10/step2")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(2));

        verify(solicitationService)
                .saveStep2(
                        eq(10),
                        any(SolicitationStep2RequestDTO.class),
                        eq(client)
                );
    }


    @Test
    void shouldReturnBadRequestWhenCepIsInvalid() throws Exception {
        String json = """
                {
                    "cep": "123",
                    "number": "10",
                    "complement": null
                }
                """;

        mockMvc.perform(
                        put("/solicitations/10/step2")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(solicitationService);
    }


    // ---------- STEP 3 ----------
    @Test
    void shouldUpdateStep3Successfully() throws Exception {
        Solicitation solicitation = createSolicitation();
        solicitation.setCurrentStep(3);

        SolicitationResponseDTO response =
                createResponseDTO(
                        SolicitationStatus.DRAFT,
                        3
                );

        when(
                solicitationService.saveStep3(
                        eq(10),
                        any(SolicitationStep3RequestDTO.class),
                        eq(client)
                )
        ).thenReturn(solicitation);

        when(solicitationMapper.toResponse(solicitation))
                .thenReturn(response);

        String json = """
                {
                    "priority": "MEDIUM",
                    "preferredDate": "2026-09-15",
                    "estimatedValue": 250.00,
                    "termsAccepted": true
                }
                """;

        mockMvc.perform(
                        put("/solicitations/10/step3")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(3));

        verify(solicitationService)
                .saveStep3(
                        eq(10),
                        any(SolicitationStep3RequestDTO.class),
                        eq(client)
                );
    }


    @Test
    void shouldReturnBadRequestWhenTermsAreNotAccepted() throws Exception {
        String json = """
                {
                    "priority": "MEDIUM",
                    "preferredDate": "2026-09-15",
                    "estimatedValue": 250.00,
                    "termsAccepted": false
                }
                """;

        mockMvc.perform(
                        put("/solicitations/10/step3")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(solicitationService);
    }


    @Test
    void shouldReturnBadRequestWhenEstimatedValueIsNegative() throws Exception {
        String json = """
                {
                    "priority": "MEDIUM",
                    "preferredDate": "2026-09-15",
                    "estimatedValue": -1,
                    "termsAccepted": true
                }
                """;

        mockMvc.perform(
                        put("/solicitations/10/step3")
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(solicitationService);
    }


    // ---------- SUBMIT ----------
    @Test
    void shouldSubmitSolicitationSuccessfully() throws Exception {
        Solicitation solicitation = createSolicitation();
        solicitation.setStatus(SolicitationStatus.SUBMITTED);
        solicitation.setCurrentStep(3);

        SolicitationResponseDTO response =
                createResponseDTO(
                        SolicitationStatus.SUBMITTED,
                        3
                );

        when(solicitationService.submit(10, client)).thenReturn(solicitation);

        when(solicitationMapper.toResponse(solicitation)).thenReturn(response);

        mockMvc.perform(post("/solicitations/10/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.currentStep").value(3));

        verify(solicitationService).submit(10, client);
        verify(solicitationMapper).toResponse(solicitation);
    }


    // ---------- GET BY ID ----------
    @Test
    void shouldGetSolicitationByIdSuccessfully() throws Exception {
        Solicitation solicitation = createSolicitation();

        SolicitationResponseDTO response = createResponseDTO(SolicitationStatus.DRAFT,0);

        when(solicitationService.findById(10, client)).thenReturn(solicitation);
        when(solicitationMapper.toResponse(solicitation)).thenReturn(response);

        mockMvc.perform(
                        get("/solicitations/10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.clientId").value(1));

        verify(solicitationService).findById(10, client);
    }


    // ---------- LIST ----------
    @Test
    void shouldListSolicitationsSuccessfully() throws Exception {
        Solicitation solicitation = createSolicitation();
        SolicitationResponseDTO response = createResponseDTO(SolicitationStatus.DRAFT,0);

        when(solicitationService.list(client, null)).thenReturn(List.of(solicitation));
        when(solicitationMapper.toResponse(solicitation)).thenReturn(response);

        mockMvc.perform(
                        get("/solicitations")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("DRAFT"));

        verify(solicitationService).list(client, null);
    }


    @Test
    void shouldListSolicitationsFilteredByStatus() throws Exception {
        Solicitation solicitation = createSolicitation();
        solicitation.setStatus(SolicitationStatus.SUBMITTED);

        SolicitationResponseDTO response =
                createResponseDTO(SolicitationStatus.SUBMITTED,3);

        when(solicitationService.list(client, SolicitationStatus.SUBMITTED)).thenReturn(List.of(solicitation));
        when(solicitationMapper.toResponse(solicitation)).thenReturn(response);

        mockMvc.perform(
                        get("/solicitations")
                                .param("status", "SUBMITTED")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"));

        verify(solicitationService).list(client,SolicitationStatus.SUBMITTED);
    }


    // ---------- MÉTODOS AUXILIARES ----------
    private Solicitation createSolicitation() {
        Solicitation solicitation = new Solicitation();

        solicitation.setId(10);
        solicitation.setClientId(1);
        solicitation.setStatus(SolicitationStatus.DRAFT);
        solicitation.setCurrentStep(0);

        return solicitation;
    }


    private SolicitationResponseDTO createResponseDTO(
            SolicitationStatus status,
            Integer currentStep
    ) {
        return new SolicitationResponseDTO(
                10,
                1,
                status,
                currentStep,

                ServiceType.INSTALLATION,
                "Instalação de ar condicionado",
                "Preciso instalar um ar condicionado na sala da minha residência.",

                "01310-100",
                "1578",
                "Apto 42",
                "Avenida Paulista",
                "Bela Vista",
                "São Paulo",
                State.SP,

                Priority.MEDIUM,
                LocalDate.of(2026, 9, 15),
                BigDecimal.valueOf(250),
                true,

                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                null,
                null
        );
    }
}