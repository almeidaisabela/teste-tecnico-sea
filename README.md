# Teste Técnico SEA — Sistema de Solicitações de Atendimento

API REST para gestão de **Solicitações de Atendimento**, construída com **Java 21 + Spring Boot**, persistência em **PostgreSQL**, indexação/busca em **Elasticsearch**, autenticação **JWT** e integração com a API pública de CEP (**ViaCEP**).

Um **Cliente (CLIENT)** cadastra uma solicitação em 3 etapas (podendo salvar e continuar depois), um **Analista (ANALYST)** avalia as solicitações dos estados (UFs) sob sua cobertura, e um **Administrador (ADMIN)** gerencia usuários internos e cobertura de UFs.

---

## Sumário

- [Stack utilizada](#stack-utilizada)
- [Como subir o projeto](#como-subir-o-projeto)
- [Migrações e usuário ADMIN inicial](#migrações-e-usuário-admin-inicial)
- [Perfis de acesso](#perfis-de-acesso)
- [Fluxo da solicitação (multi-step)](#fluxo-da-solicitação-multi-step)
- [Endpoints](#endpoints)
- [Busca no Elasticsearch](#busca-no-elasticsearch)
- [Auditoria (AOP)](#auditoria-aop)
- [Tratamento de erros](#tratamento-de-erros)
- [Testes](#testes)
- [Exemplos de uso (curl)](#exemplos-de-uso-curl)
- [Documentação OpenAPI/Swagger](#documentação-openapiswagger)

---

## Stack utilizada

- **Java 21** + **Spring Boot**
- **Spring Data JPA** + **PostgreSQL** (persistência transacional)
- **Spring Data Elasticsearch** (indexação e busca com filtros + paginação)
- **Flyway** (versionamento de schema)
- **Spring Security** + **JWT** (autenticação/autorização, via `java-jwt`)
- **Spring AOP / AspectJ** (auditoria)
- **Bean Validation** (validações de entrada)
- **MapStruct** (mapeamento entidade ↔ DTO)
- **springdoc-openapi** (documentação Swagger/OpenAPI)
- **ViaCEP** (integração pública de consulta de CEP)
- **Docker** + **Docker Compose** (app + postgres + elasticsearch)
- **JUnit / Testcontainers** (testes unitários e de integração)

---

## Como subir o projeto

### Pré-requisitos
- Docker e Docker Compose

### Passo a passo

1. Clone o repositório:
   ```bash
   git clone https://github.com/almeidaisabela/teste-tecnico-sea.git
   cd teste-tecnico-sea
   ```

2. Crie o arquivo `.env` na raiz do projeto (baseado no `.env.EXEMPLE`), preenchendo **todas** as variáveis abaixo:
   ```env
   POSTGRES_USER=admin
   POSTGRES_PASSWORD=uma-senha-forte
   POSTGRES_PORT=4003
   ELASTICSEARCH_PORT=4004
   ```

3. Suba a aplicação (API + PostgreSQL + Elasticsearch):
   ```bash
   docker compose up --build
   ```

4. Aguarde os logs indicarem que a aplicação subiu com sucesso:
   ```
   Started TesteTecnicoSeaApplication in X.XXX seconds
   ```

A API estará disponível em `http://localhost:8080`.

---

## Migrações e usuário ADMIN inicial

As migrações são gerenciadas via **Flyway** e executadas automaticamente na subida da aplicação (`spring.flyway.enabled=true`), não é necessário rodar nada manualmente.

Elas criam as tabelas `users`, `analyst_coverage_state`, `solicitations` e `audit_logs`, e populam um **usuário ADMIN inicial** via seed (migration `V3`):

- **Email:** `admin@sistema.com`
- **Senha:** `senha123`

Use essas credenciais no endpoint `POST /auth/login` para obter um token JWT com permissões de administrador e, a partir dele, criar os demais usuários internos (`ANALYST`/`ADMIN`).

---

## Perfis de acesso

| Perfil    | Permissões |
|-----------|------------|
| `CLIENT`  | Se autocadastra (`/auth/register`); cria/edita **apenas as próprias** solicitações; salva rascunho e continua depois; envia para análise |
| `ANALYST` | Lista e analisa solicitações **apenas das UFs** sob sua cobertura; não cria usuários |
| `ADMIN`   | Acesso total; único perfil que cria usuários internos (`ANALYST`/`ADMIN`); define quais UFs cada analista cobre |

Regras de autorização aplicadas via Spring Security + filtro JWT (`SecurityConfig` / `SecurityFilter`):
- `POST /auth/register` e `POST /auth/login` — públicos
- `/admin/**` — somente `ADMIN`
- `/analyst/**` — `ANALYST` e `ADMIN`
- Demais rotas — exigem autenticação; regras de *ownership* (dono da solicitação) e de cobertura por UF são aplicadas na camada de serviço

---

## Fluxo da solicitação (multi-step)

Uma solicitação é criada vazia (`status = DRAFT`) e preenchida em 3 etapas, cada uma salva de forma independente. O cliente pode sair a qualquer momento e retomar de onde parou.

### Step 1 — Dados básicos (`PUT /solicitations/{id}/step1`)
- `serviceType`: `INSTALLATION` | `MAINTENANCE` | `INSPECTION`
- `title`: 3 a 80 caracteres
- `description`: 20 a 1000 caracteres

### Step 2 — Endereço + integração de CEP (`PUT /solicitations/{id}/step2`)
- `cep`: formato `00000-000` ou `00000000`
- `number`: 1 a 20 caracteres (aceita valores como `12A`)
- `complement`: opcional, até 100 caracteres
- Ao informar o `cep`, a API consulta o **ViaCEP** e pré-preenche `street`, `neighborhood`, `city` e `state`. Se o CEP for inválido ou a consulta falhar, o Step 2 **não é concluído**.

### Step 3 — Confirmação e dados finais (`PUT /solicitations/{id}/step3`)
- `priority`: `LOW` | `MEDIUM` | `HIGH`
- `preferredDate`: obrigatória, não pode ser no passado
- `estimatedValue`: obrigatório, `>= 0` (e `>= 100` quando `priority = HIGH`)
- `termsAccepted`: deve ser `true`

Cada etapa só pode ser salva enquanto `status = DRAFT`.

### Envio para análise (`POST /solicitations/{id}/submit`)
Revalida a completude e a coerência de **todas** as etapas antes de mudar o status para `SUBMITTED`. Só o cliente dono pode submeter, e só enquanto `status = DRAFT`. Após o envio, os dados ficam bloqueados para edição pelo cliente.

---

## Endpoints

### Autenticação (`/auth`)
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/auth/register` | Público | Autocadastro (sempre cria `CLIENT`) |
| POST | `/auth/login` | Público | Autentica e retorna token JWT |

### Solicitações — Cliente (`/solicitations`)
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/solicitations` | `CLIENT` | Cria uma solicitação vazia em `DRAFT` |
| PUT | `/solicitations/{id}/step1` | `CLIENT` (dono) | Salva/atualiza a Etapa 1 |
| PUT | `/solicitations/{id}/step2` | `CLIENT` (dono) | Salva/atualiza a Etapa 2 (consulta CEP) |
| PUT | `/solicitations/{id}/step3` | `CLIENT` (dono) | Salva/atualiza a Etapa 3 |
| POST | `/solicitations/{id}/submit` | `CLIENT` (dono) | Envia a solicitação para análise |
| GET | `/solicitations/{id}` | `CLIENT` (dono) | Consulta uma solicitação |
| GET | `/solicitations?status=` | `CLIENT` | Lista as próprias solicitações (filtro opcional por status) |

### Análise — Analista/Admin (`/analyst`)
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/analyst/solicitations/{id}` | `ANALYST` (na sua UF) / `ADMIN` | Consulta uma solicitação |
| POST | `/analyst/solicitations/{id}/start` | `ANALYST` / `ADMIN` | `SUBMITTED` → `IN_REVIEW` |
| POST | `/analyst/solicitations/{id}/decide` | `ANALYST` / `ADMIN` | Aprova ou rejeita (`APPROVE`/`REJECT`) |
| GET | `/analyst/solicitations/search` | `ANALYST` / `ADMIN` | Busca com filtros e paginação (Elasticsearch) |

### Administração — Admin (`/admin`)
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/admin/users` | `ADMIN` | Cria usuário interno (`ANALYST`/`ADMIN`) |
| PUT | `/admin/users/{id}/coverage` | `ADMIN` | Define as UFs cobertas por um analista |
| GET | `/admin/users/{id}/coverage` | `ADMIN` | Consulta as UFs cobertas por um analista |

---

## Busca no Elasticsearch

A entidade **Solicitation** é indexada automaticamente ao ser criada, atualizada em cada etapa, submetida e decidida, com os campos: `id`, `clientId`, `status`, `serviceType`, `title`, `description`, `state`, `city`, `priority`, `createdAt`, `submittedAt`.

Endpoint: `GET /analyst/solicitations/search`

| Parâmetro | Descrição |
|---|---|
| `q` | Busca textual em `title` e `description` |
| `status` | Um ou mais status (`DRAFT`, `SUBMITTED`, `IN_REVIEW`, `APPROVED`, `REJECTED`) |
| `serviceType` | Filtro opcional |
| `priority` | Filtro opcional |
| `state` | UF; para `ADMIN` é opcional, para `ANALYST` é **forçado** à sua cobertura, mesmo que outro valor seja enviado |
| `dateFrom`, `dateTo` | Intervalo aplicado sobre `submittedAt` |
| `page`, `size` | Paginação (default: `page=0`, `size=20`) |
| `sort` | Ex.: `submittedAt,desc` |

Resposta:
```json
{
  "items": [ /* SolicitationResponseDTO[] */ ],
  "page": 0,
  "size": 20,
  "total": 42
}
```

---

## Auditoria (AOP)

Uso de **Spring AOP** para auditar automaticamente ações críticas, via anotação customizada `@Audit(action = "...")`, aplicada em:

- `POST /solicitations/{id}/submit` → `SUBMIT_SOLICITATION`
- `POST /analyst/solicitations/{id}/decide` → `DECIDE_SOLICITATION`
- `POST /admin/users` → `CREATE_USER`

Cada execução gera um registro na tabela `audit_logs`, contendo: `user_id`, `role`, `action`, `entity_id`, `duration_ms`, `success` e `error_message` (quando aplicável).

---

## Tratamento de erros

Erros seguem um formato padronizado (`GlobalExceptionHandler`):

```json
{
  "timestamp": "2026-09-03T10:15:00",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Erro de validação",
  "path": "/solicitations/1/step1",
  "fieldErrors": [
    { "field": "title", "message": "O TÍTULO deve ter entre 3 e 80 caracteres." }
  ]
}
```

---

## Testes

O projeto conta com testes unitários (regras de negócio de serviços) e um teste de integração (fluxo de solicitação de ponta a ponta, com Testcontainers):

```bash
./mvnw test
```

---

## Exemplos de uso (curl)

> Ajuste `http://localhost:8080` caso rode em outra porta.

### 1. Login como ADMIN (usuário seed)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sistema.com","password":"admin123"}'
```

### 2. Admin cria um Analista
```bash
curl -X POST http://localhost:8080/admin/users \
  -H "Authorization: Bearer <TOKEN_ADMIN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "José Silva",
    "email": "jose.analista@email.com",
    "password": "senha123",
    "role": "ANALYST"
  }'
```

### 3. Admin define a cobertura de UF do Analista
```bash
curl -X PUT http://localhost:8080/admin/users/2/coverage \
  -H "Authorization: Bearer <TOKEN_ADMIN>" \
  -H "Content-Type: application/json" \
  -d '{"states": ["SP", "RJ"]}'
```

### 4. Cliente se autocadastra
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ana Souza",
    "email": "ana@email.com",
    "password": "senha123"
  }'
```

### 5. Cliente faz login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@email.com","password":"senha123"}'
```

### 6. Cliente cria uma solicitação (DRAFT)
```bash
curl -X POST http://localhost:8080/solicitations \
  -H "Authorization: Bearer <TOKEN_CLIENT>"
```

### 7. Cliente preenche o Step 1
```bash
curl -X PUT http://localhost:8080/solicitations/1/step1 \
  -H "Authorization: Bearer <TOKEN_CLIENT>" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceType": "INSTALLATION",
    "title": "Instalação de ar condicionado",
    "description": "Preciso instalar um ar condicionado split de 12000 BTUs na sala de estar."
  }'
```

### 8. Cliente preenche o Step 2 (consulta CEP)
```bash
curl -X PUT http://localhost:8080/solicitations/1/step2 \
  -H "Authorization: Bearer <TOKEN_CLIENT>" \
  -H "Content-Type: application/json" \
  -d '{
    "cep": "01310-100",
    "number": "1578",
    "complement": "Apto 42"
  }'
```

### 9. Cliente preenche o Step 3
```bash
curl -X PUT http://localhost:8080/solicitations/1/step3 \
  -H "Authorization: Bearer <TOKEN_CLIENT>" \
  -H "Content-Type: application/json" \
  -d '{
    "priority": "MEDIUM",
    "preferredDate": "2026-09-20",
    "estimatedValue": 250.00,
    "termsAccepted": true
  }'
```

### 10. Cliente envia para análise
```bash
curl -X POST http://localhost:8080/solicitations/1/submit \
  -H "Authorization: Bearer <TOKEN_CLIENT>"
```

### 11. Analista busca solicitações
```bash
curl -X GET "http://localhost:8080/analyst/solicitations/search?status=SUBMITTED&page=0&size=20&sort=submittedAt,desc" \
  -H "Authorization: Bearer <TOKEN_ANALYST>"
```

### 12. Analista decide (aprova/rejeita)
```bash
curl -X POST http://localhost:8080/analyst/solicitations/1/decide \
  -H "Authorization: Bearer <TOKEN_ANALYST>" \
  -H "Content-Type: application/json" \
  -d '{
    "decision": "APPROVE",
    "comment": "Documentação completa e endereço validado. Aprovado para execução."
  }'
```

---

## Documentação OpenAPI/Swagger

Com a aplicação em execução:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
