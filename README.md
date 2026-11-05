# AgentFlow AI

**Plataforma de gestão de workflows e base para automações com agentes de inteligência artificial.**

**Versão atual:** MVP v0.1.0 — núcleo de workflows | **Linguagem:** Java 21 | **Interface:** React + TypeScript

> **Estado real do produto:** o MVP permite cadastrar workflows e controlar manualmente o ciclo de vida das execuções. **Não há agentes de IA, ferramentas MCP, filas, autenticação ou integrações externas implementadas nesta versão.** Essas capacidades estão planejadas no roadmap.

## Sumário

1. [Visão geral](#visão-geral)
2. [Funcionalidades e maturidade](#funcionalidades-e-maturidade)
3. [Tecnologias](#tecnologias)
4. [Arquitetura](#arquitetura)
5. [Instalação e execução](#instalação-e-execução)
6. [API REST](#api-rest)
7. [Regras de negócio](#regras-de-negócio)
8. [Modelo de dados e DER](#modelo-de-dados-e-der)
9. [Máquina de estados](#máquina-de-estados)
10. [Testes e integração contínua](#testes-e-integração-contínua)
11. [Segurança e limitações](#segurança-e-limitações)
12. [Roadmap](#roadmap)
13. [Documentação técnica](#documentação-técnica)
14. [Contribuição e licença](#contribuição-e-licença)

## Visão geral

O **AgentFlow AI** organiza processos reutilizáveis (*workflows*) e registra suas execuções. O objetivo de longo prazo é evoluir essa base para uma plataforma de agentes especializados com execução controlada, aprovação humana, trilha de auditoria e observabilidade.

**Problema atendido pelo MVP:** registrar e acompanhar workflows e seus estados sem depender de execução automatizada real.

**Fluxo funcional implementado:**

1. O usuário cadastra um workflow.
2. O backend persiste o workflow com estado ACTIVE.
3. O usuário solicita uma nova execução; nasce uma execução PENDING.
4. O usuário inicia a execução; seu estado passa a RUNNING.
5. O usuário conclui a execução; seu estado passa a COMPLETED.
6. A interface consulta os registros persistidos no PostgreSQL.

A palavra "Executar" no dashboard significa **criar um registro de execução**, não disparar um robô ou um agente de IA.

## Funcionalidades e maturidade

| Capacidade | Situação | Observação |
|---|---|---|
| Cadastrar e listar workflows | ✅ Implementado | Validação básica de entrada |
| Criar execuções | ✅ Implementado | Estado inicial PENDING |
| Iniciar e concluir execuções | ✅ Implementado | Transições de estado validadas |
| Armazenamento em PostgreSQL | ✅ Implementado | JPA + Flyway |
| Interface web responsiva | ✅ Implementado | React + TypeScript |
| Endpoint de saúde | ✅ Implementado | Spring Boot Actuator |
| Datas distintas de criação/início/fim | ✅ Nesta proposta | Migração Flyway V2 |
| CI de backend e frontend | ✅ Configurado nesta proposta | Execução remota ainda depende do GitHub Actions |
| Autenticação, autorização e auditoria | 🕒 Planejado | Obrigatório antes de exposição pública |
| Orquestração assíncrona, retry, filas | 🕒 Planejado | Sem execução externa no MVP |
| Aprovação humana | 🕒 Planejado | Ainda não há mecanismo de aprovação |
| Spring AI / MCP / agentes autônomos | 🕒 Planejado | Não instalado no backend atual |
| Observabilidade distribuída / AWS | 🕒 Planejado | Sem garantias de produção |

## Tecnologias

| Camada | Tecnologias |
|---|---|
| Backend | Java 21, Spring Boot 3.5.6, Spring Web, Spring Data JPA, Bean Validation |
| Persistência | PostgreSQL 17, Flyway, Hibernate |
| Interface | React 18, TypeScript, Vite |
| Infraestrutura local | Docker Compose, Nginx |
| Qualidade | JUnit 5 (via Spring Boot Test), Maven, compilação TypeScript, GitHub Actions |
| Futuro | Spring AI, MCP, mensageria, OpenTelemetry, infraestrutura cloud |

## Arquitetura

~~~mermaid
flowchart LR
    U[Usuário] --> F[Frontend React + TypeScript]
    F -->|HTTP /api/v1| N[Nginx / Proxy]
    N --> C[Spring Boot REST Controller]
    C --> R[Spring Data JPA Repositories]
    R --> DB[(PostgreSQL 17)]
    M[Flyway V1 + V2] -->|Migrações| DB
    C --> E[Entidades e regras de transição]
~~~

**Padrão atual:** aplicação web cliente-servidor, backend monolítico e persistência relacional. O controlador REST acessa os repositórios diretamente; a extração de uma camada de serviços faz parte da evolução arquitetural. Não há microsserviços nem processador de filas nesta versão.

Leitura complementar: [Arquitetura e decisões técnicas](docs/architecture.md).

### Estrutura do repositório

~~~text
agentflow-ai/
├── .github/workflows/ci.yml
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/java/com/agentflow/workflow/
│       ├── main/resources/application.yml
│       ├── main/resources/db/migration/
│       │   ├── V1__init.sql
│       │   └── V2__execution_lifecycle.sql
│       └── test/java/com/agentflow/workflow/
├── frontend/
│   ├── src/main.tsx
│   ├── src/style.css
│   ├── nginx.conf
│   └── package.json
├── docs/
│   ├── architecture.md
│   ├── business-rules.md
│   ├── data-model.md
│   ├── api.md
│   ├── security-and-quality.md
│   └── adr/0001-monolith-first.md
├── compose.yaml
└── README.md
~~~

## Instalação e execução

### Pré-requisitos

- Docker Desktop ou Docker Engine com Compose V2.
- Portas disponíveis no host: **3008** (dashboard), **8088** (API) e **5436** (PostgreSQL).

1. Clone o projeto:

~~~bash
git clone https://github.com/juceliocoelho2022/agentflow-ai.git
cd agentflow-ai
~~~

2. Suba os serviços (a compilação inicial pode levar algum tempo):

~~~bash
docker compose up --build -d
docker compose ps
~~~

3. Acesse:
   - **Dashboard:** http://localhost:3008
   - **API:** http://localhost:8088/api/v1/workflows
   - **Health:** http://localhost:8088/actuator/health

4. Visualize logs ou encerre:

~~~bash
docker compose logs -f backend
docker compose down
~~~

> **Atenção:** executar docker compose down -v remove o volume do PostgreSQL e **apaga os dados locais**. Não execute esse comando se houver dados a preservar. As credenciais de exemplo do Compose são destinadas exclusivamente a desenvolvimento.

### Execução de testes localmente

Com JDK 21 e Maven instalados:

~~~bash
cd backend
mvn test
~~~

Para verificar o build do frontend, com Node.js 22 e npm:

~~~bash
cd frontend
npm install
npm run build
~~~

A configuração atual do Vite não possui proxy próprio para a API; para uso integrado da interface, prefira o Docker Compose com Nginx.

## API REST

Prefixo: **/api/v1/workflows**.

| Método | Caminho | Resultado esperado |
|---|---|---|
| GET | / | Lista de workflows — 200 |
| POST | / | Cadastro — 201 |
| POST | /{workflowId}/executions | Nova execução PENDING — 201 |
| GET | /{workflowId}/executions | Histórico — 200 |
| POST | /executions/{executionId}/start | PENDING → RUNNING — 200 |
| POST | /executions/{executionId}/complete | RUNNING → COMPLETED — 200 |

**Cadastro:**

~~~http
POST /api/v1/workflows
Content-Type: application/json

{
  "name": "Aprovação de pedido",
  "description": "Registro de um processo de aprovação"
}
~~~

**Respostas de erro mais relevantes:** 400 (entrada inválida), 404 (recurso inexistente), 409 (transição inválida). A aplicação ainda não possui contrato centralizado e versionado de mensagens de erro.

Os exemplos completos em cURL e PowerShell e os campos de retorno estão em [Contrato da API](docs/api.md).

## Regras de negócio

| ID | Regra | Implementação |
|---|---|---|
| RN-001 | Nome do workflow obrigatório, não vazio e com até 120 caracteres | Bean Validation |
| RN-002 | Descrição opcional com até 1.000 caracteres | Bean Validation / banco |
| RN-003 | Novo workflow criado no estado ACTIVE | Entidade Workflow |
| RN-004 | Cada workflow pode possuir zero ou várias execuções | Relacionamento 1:N |
| RN-005 | Toda execução inicia com status PENDING | Entidade WorkflowExecution |
| RN-006 | Apenas PENDING pode transicionar para RUNNING | Regra de domínio |
| RN-007 | Apenas RUNNING pode transicionar para COMPLETED | Regra de domínio |
| RN-008 | Transições inválidas resultam em conflito HTTP 409 | Controller |
| RN-009 | Operações com identificadores inexistentes retornam HTTP 404 | Controller |
| RN-010 | Criação, início e conclusão possuem instantes separados | Entidade + Flyway V2 |
| RN-011 | Identificadores são UUID e dados sobrevivem a reinicializações | JPA / PostgreSQL |
| RN-012 | Execução não aciona sistemas externos ou modelos de IA | Limitação explícita |

Veja o catálogo completo com pré-condições, pós-condições e pendências em [Regras de Negócio](docs/business-rules.md).

## Modelo de dados e DER

~~~mermaid
erDiagram
    WORKFLOWS ||--o{ WORKFLOW_EXECUTIONS : "possui"
    WORKFLOWS {
        UUID id PK
        VARCHAR name
        VARCHAR description
        VARCHAR status
        TIMESTAMPTZ created_at
    }
    WORKFLOW_EXECUTIONS {
        UUID id PK
        UUID workflow_id FK
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ started_at
        TIMESTAMPTZ finished_at
    }
~~~

**Integridade:** chave estrangeira NOT NULL, estados controlados por CHECK no banco após V2 e índice por workflow/data de criação. A migração V2 separa data de criação e início. Para execuções históricas RUNNING/COMPLETED, a data real do início não pode ser reconstruída com exatidão a partir da V1.

Detalhes de tabelas, campos, restrições e migrações: [Modelo de Dados](docs/data-model.md).

## Máquina de estados

~~~mermaid
stateDiagram-v2
    [*] --> PENDING : criar execução
    PENDING --> RUNNING : iniciar
    RUNNING --> COMPLETED : concluir
    COMPLETED --> [*]
~~~

Não existem na versão atual os estados FAILED, CANCELLED ou RETRYING. Reenvios, retries automáticos e idempotência ainda não estão implementados.

## Testes e integração contínua

O projeto contém testes unitários sobre a máquina de estados: criação pendente, transições válidas, timestamps e rejeição de transições inválidas. A configuração [GitHub Actions](.github/workflows/ci.yml) define:

- **Backend:** Java 21 com Maven verify;
- **Frontend:** Node 22 com instalação de dependências e TypeScript/Vite build.

**Transparência:** estes testes e o pipeline foram configurados, mas a aprovação em CI deve ser verificada na aba Actions após publicar a branch. Ainda não há cobertura abrangente de integração com PostgreSQL, testes end-to-end ou garantias de qualidade de produção.

Plano de qualidade e segurança: [Segurança e Qualidade](docs/security-and-quality.md).

## Segurança e limitações

**Somente para ambiente local/de desenvolvimento.** O MVP atual não possui login, JWT, autorização por usuário, trilha imutável de auditoria, rate limiting nem gerenciamento robusto de segredos. O arquivo Compose contém credenciais de desenvolvimento e publica portas no host.

Não exponha a API ou o banco à internet. Antes de implantar em produção, implemente ao menos autenticação/autorização, proteção de credenciais, restrições de rede, revisão de CORS/CSRF, limites de requisições, testes de integração, backups e monitoramento.

## Roadmap

| Etapa | Objetivo | Status |
|---|---|---|
| v0.1 | CRUD inicial de workflows, execuções e PostgreSQL | Implementado |
| v0.1.x | Documentação, integridade temporal, CI, testes adicionais | Proposta nesta branch |
| v0.2 | JWT, RBAC, auditoria e serviços de aplicação | Planejado |
| v0.3 | Etapas, jobs assíncronos, retries e idempotência | Planejado |
| v0.4 | Aprovação humana, filas e trilha de decisão | Planejado |
| v0.5 | Spring AI, tools allowlisted, agentes Java/financeiro/educacional | Planejado |
| v0.6 | Métricas, traces, logs correlacionados e SLOs | Planejado |
| v0.7 | Integração cloud opcional e políticas de custos | Planejado |

Essas versões representam objetivos, não funcionalidades disponíveis hoje.

## Documentação técnica

- [Arquitetura atual, diagramas e decisões](docs/architecture.md)
- [Regras de negócio e critérios de aceitação](docs/business-rules.md)
- [DER, restrições e evolução do esquema](docs/data-model.md)
- [Contratos da API e exemplos](docs/api.md)
- [Segurança, testes, riscos e requisitos não funcionais](docs/security-and-quality.md)
- [ADR-0001: monólito modular como ponto de partida](docs/adr/0001-monolith-first.md)

## Contribuição e licença

Sugestões e correções podem ser discutidas por *issues* e *pull requests*. Para mudanças futuras, prefira branches curtas, commits descritivos, testes automatizados e documentação atualizada.

**Licença:** o repositório não possui arquivo LICENSE nesta versão. Isso **não** significa autorização irrestrita para reutilizar ou redistribuir o código. Defina uma licença explicitamente antes de compartilhar sob termos de código aberto.

---

**AgentFlow AI** — construído com foco em evolução incremental, rastreabilidade e engenharia de software.

