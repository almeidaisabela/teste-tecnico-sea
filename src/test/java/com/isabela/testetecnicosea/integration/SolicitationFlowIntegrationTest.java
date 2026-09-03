package com.isabela.testetecnicosea.integration;

import org.junit.jupiter.api.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.isabela.testetecnicosea.model.entity.User;
import com.isabela.testetecnicosea.model.enums.UserRole;
import com.isabela.testetecnicosea.model.dto.ViaCepResponseDTO;
import com.isabela.testetecnicosea.repository.UserRepository;
import com.isabela.testetecnicosea.service.CepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SolicitationFlowIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("solicitacoes_de_atendimento_test")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CepService cepService;

    private static final String CLIENT_EMAIL = "cliente.integracao@teste.com";
    private static final String CLIENT_PASSWORD = "senhaForte123";

    private static final String OTHER_CLIENT_EMAIL = "outro.cliente@teste.com";
    private static final String OTHER_CLIENT_PASSWORD = "outraSenha123";

    private static final String ADMIN_EMAIL = "admin.integracao@teste.com";
    private static final String ADMIN_PASSWORD = "adminSenha123";

    private static final String ANALYST_EMAIL = "analista.integracao@teste.com";
    private static final String ANALYST_PASSWORD = "analistaSenha123";

    private String clientToken;
    private String otherClientToken;
    private String adminToken;
    private String analystToken;

    private Integer solicitationId;
    private Integer analystId;


    @BeforeAll
    void setUpAdmin() throws Exception {
        User admin = new User();
        admin.setName("Admin Integração");
        admin.setEmail(ADMIN_EMAIL);
        admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setRole(UserRole.ADMIN);
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now());

        userRepository.save(admin);

        adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }


    @BeforeEach
    void setUpCepMock() {

        when(cepService.findAdress("01310-100"))
                .thenReturn(new ViaCepResponseDTO(
                        "Avenida Paulista",
                        "Bela Vista",
                        "São Paulo",
                        "SP",
                        false
                ));

        when(cepService.findAdress("00000-000"))
                .thenReturn(new ViaCepResponseDTO(
                        null,
                        null,
                        null,
                        null,
                        true
                ));
    }


    // ---------- AUTENTICAÇÃO ----------
    @Test
    @Order(1)
    void deveRegistrarNovoClienteComoClient() throws Exception {
        String json = """
                {
                    "name": "Cliente de Integração",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(CLIENT_EMAIL, CLIENT_PASSWORD);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(CLIENT_EMAIL))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }


    @Test
    @Order(2)
    void deveFazerLoginDoClienteERetornarToken() throws Exception {
        clientToken = login(CLIENT_EMAIL, CLIENT_PASSWORD);
        assertThat(clientToken).isNotBlank();
    }


    @Test
    @Order(3)
    void naoDeveAutenticarComCredenciaisInvalidas() throws Exception {
        String json = """
                {
                    "email": "%s",
                    "password": "senhaErrada123"
                }
                """.formatted(CLIENT_EMAIL);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }


    // ---------- MULTI-STEP ----------
    @Test
    @Order(4)
    void deveCriarSolicitacaoComoRascunho() throws Exception {
        MvcResult result = mockMvc.perform(authorized(post("/solicitations"), clientToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.currentStep").value(0))
                .andReturn();

        solicitationId = extractInt(result, "id");
        assertThat(solicitationId).isNotNull();
    }


    @Test
    @Order(5)
    void deveSalvarStep1EAvancarCurrentStepParaUm() throws Exception {
        String json = """
                {
                    "serviceType": "INSTALLATION",
                    "title": "Instalação de ar condicionado",
                    "description": "Preciso instalar um ar condicionado split de 12000 BTUs na sala de estar."
                }
                """;

        mockMvc.perform(authorized(put("/solicitations/" + solicitationId + "/step1"), clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }


    @Test
    @Order(6)
    void deveSalvarStep2ConsultandoCepEAvancarCurrentStepParaDois() throws Exception {
        String json = """
                {
                    "cep": "01310-100",
                    "number": "1578",
                    "complement": "Apto 42"
                }
                """;

        mockMvc.perform(authorized(put("/solicitations/" + solicitationId + "/step2"), clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(2))
                .andExpect(jsonPath("$.street").value("Avenida Paulista"))
                .andExpect(jsonPath("$.city").value("São Paulo"))
                .andExpect(jsonPath("$.state").value("SP"));
    }


    @Test
    @Order(7)
    void deveSalvarStep3EAvancarCurrentStepParaTres() throws Exception {
        String json = """
                {
                    "priority": "MEDIUM",
                    "preferredDate": "%s",
                    "estimatedValue": 250.00,
                    "termsAccepted": true
                }
                """.formatted(LocalDate.now().plusDays(10));

        mockMvc.perform(authorized(put("/solicitations/" + solicitationId + "/step3"), clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(3));
    }


    // ---------- SUBMIT E OWNERSHIP ----------
    @Test
    @Order(8)
    void deveEnviarParaAnaliseAposCompletarAsTresEtapas() throws Exception {
        mockMvc.perform(authorized(post("/solicitations/" + solicitationId + "/submit"), clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.submittedAt").exists());
    }


    @Test
    @Order(9)
    void naoDevePermitirClienteEditarSolicitacaoJaEnviada() throws Exception {
        String json = """
                {
                    "serviceType": "MAINTENANCE",
                    "title": "Tentativa de edição pós-envio",
                    "description": "Essa alteração não deveria ser permitida pois já foi enviada."
                }
                """;

        mockMvc.perform(authorized(put("/solicitations/" + solicitationId + "/step1"), clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }


    @Test
    @Order(10)
    void naoDevePermitirOutroClienteAcessarSolicitacaoDeTerceiro() throws Exception {
        registerClient(OTHER_CLIENT_EMAIL, OTHER_CLIENT_PASSWORD);
        otherClientToken = login(OTHER_CLIENT_EMAIL, OTHER_CLIENT_PASSWORD);

        mockMvc.perform(authorized(get("/solicitations/" + solicitationId), otherClientToken))
                .andExpect(status().isForbidden());
    }


    // ---------- ADMIN E COBERTURA DE UF ----------
    @Test
    @Order(11)
    void adminDeveCriarUsuarioAnalista() throws Exception {
        String json = """
                {
                    "name": "Analista de Integração",
                    "email": "%s",
                    "password": "%s",
                    "role": "ANALYST"
                }
                """.formatted(ANALYST_EMAIL, ANALYST_PASSWORD);

        MvcResult result = mockMvc.perform(authorized(post("/admin/users"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ANALYST"))
                .andReturn();

        analystId = extractInt(result, "id");
        analystToken = login(ANALYST_EMAIL, ANALYST_PASSWORD);
    }


    @Test
    @Order(12)
    void analistaSemCoberturaDeUfNaoDeveAcessarSolicitacao() throws Exception {
        mockMvc.perform(authorized(get("/analyst/solicitations/" + solicitationId), analystToken))
                .andExpect(status().isForbidden());
    }


    @Test
    @Order(13)
    void adminDeveConfigurarCoberturaDeUfDoAnalista() throws Exception {
        String json = """
                {
                    "states": ["SP"]
                }
                """;

        mockMvc.perform(authorized(put("/admin/users/" + analystId + "/coverage"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.states[0]").value("SP"));
    }


    // ---------- ANÁLISE ----------
    @Test
    @Order(14)
    void analistaComCoberturaDeveVisualizarSolicitacao() throws Exception {
        mockMvc.perform(authorized(get("/analyst/solicitations/" + solicitationId), analystToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitationId))
                .andExpect(jsonPath("$.state").value("SP"));
    }


    @Test
    @Order(15)
    void analistaDeveIniciarAnaliseMudandoStatusParaInReview() throws Exception {
        mockMvc.perform(authorized(post("/analyst/solicitations/" + solicitationId + "/start"), analystToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_REVIEW"));
    }


    @Test
    @Order(16)
    void analistaDeveAprovarSolicitacao() throws Exception {
        String json = """
                {
                    "decision": "APPROVE",
                    "comment": "Endereço validado e documentação completa. Aprovado para execução."
                }
                """;

        mockMvc.perform(authorized(post("/analyst/solicitations/" + solicitationId + "/decide"), analystToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.analyzedBy").value(analystId))
                .andExpect(jsonPath("$.analysisComment").isNotEmpty());
    }


    @Test
    @Order(17)
    void clienteDeveVisualizarSolicitacaoAprovada() throws Exception {
        mockMvc.perform(authorized(get("/solicitations/" + solicitationId), clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }


    @Test
    @Order(18)
    void naoDevePermitirAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/solicitations"))
                .andExpect(status().isForbidden());
    }


    // ---------- REGRAS DE NEGÓCIO ISOLADAS ----------
    @Test
    @Order(19)
    void naoDeveConcluirStep2ComCepInvalido() throws Exception {
        Integer id = createDraftAtStep1();

        String json = """
                {
                    "cep": "00000-000",
                    "number": "10",
                    "complement": null
                }
                """;

        mockMvc.perform(authorized(put("/solicitations/" + id + "/step2"), clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        mockMvc.perform(authorized(get("/solicitations/" + id), clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(1));
    }


    @Test
    @Order(20)
    void naoDeveConcluirStep3ComPrioridadeHighEValorAbaixoDeCem() throws Exception {
        Integer id = createDraftAtStep1();

        mockMvc.perform(authorized(put("/solicitations/" + id + "/step2"), clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "cep": "01310-100",
                                    "number": "10",
                                    "complement": null
                                }
                                """))
                .andExpect(status().isOk());

        String step3Json = """
                {
                    "priority": "HIGH",
                    "preferredDate": "%s",
                    "estimatedValue": 50.00,
                    "termsAccepted": true
                }
                """.formatted(LocalDate.now().plusDays(5));

        mockMvc.perform(authorized(put("/solicitations/" + id + "/step3"), clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(step3Json))
                .andExpect(status().isBadRequest());
    }


    // ---------- HELPERS ----------
    private Integer createDraftAtStep1() throws Exception {
        MvcResult created = mockMvc.perform(authorized(post("/solicitations"), clientToken))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = extractInt(created, "id");

        mockMvc.perform(authorized(put("/solicitations/" + id + "/step1"), clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "serviceType": "MAINTENANCE",
                                    "title": "Manutenção preventiva",
                                    "description": "Revisão geral do sistema de climatização do apartamento."
                                }
                                """))
                .andExpect(status().isOk());

        return id;
    }


    private void registerClient(String email, String password) throws Exception {
        String json = """
                {
                    "name": "Cliente Teste",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }


    private String login(String email, String password) throws Exception {
        String json = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = jsonMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }


    private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }

    private Integer extractInt(MvcResult result, String field) throws Exception {
        JsonNode body = jsonMapper.readTree(result.getResponse().getContentAsString());
        return body.get(field).asInt();
    }


}