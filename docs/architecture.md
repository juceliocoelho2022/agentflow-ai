# Arquitetura de software — AgentFlow AI

> **Escopo:** implementação atual (v0.1.x). O projeto ainda não possui agentes executores de IA nem orquestração assíncrona.

## Contexto e fronteiras

~~~mermaid
flowchart LR
    P[Pessoa usuária] -->|navegador| W[AgentFlow AI]
    W -->|leitura e escrita| DB[(PostgreSQL)]
    W -.->|futuro| M[Modelos de IA]
    W -.->|futuro| X[Sistemas externos]
~~~

As dependências pontilhadas são **planejadas**, não estão em execução.

## Contêineres implantados via Docker Compose

~~~mermaid
flowchart TB
    browser["Navegador web"]
    nginx["frontend: Nginx + build React"]
    api["backend: Spring Boot 3.5.6 / Java 21"]
    pg[("db: PostgreSQL 17")]
    browser -->|HTTP :3008| nginx
    nginx -->|proxy /api → backend:8080| api
    api -->|JDBC / JPA| pg
    api -->|Flyway V1, V2| pg
~~~

O frontend não se conecta diretamente ao PostgreSQL. Os containers se comunicam pela rede interna do Compose.

## Organização lógica presente no código

~~~mermaid
flowchart LR
    WC[WorkflowController] --> WR[WorkflowRepository]
    WC --> ER[WorkflowExecutionRepository]
    WC --> ENT["Workflow / WorkflowExecution"]
    WR --> PG[(PostgreSQL)]
    ER --> PG
~~~

- **Controller:** valida JSON e devolve DTOs, decide status HTTP.
- **Entidades JPA:** concentram estados, transições e marcações temporais.
- **Repositories:** consultas e persistência usando Spring Data.
- **Flyway:** cria e evolui schema; Hibernate está em modo validate.
- **React:** opera cadastro, listagem e transições pela API.

Não há ainda camadas Service/UseCase independentes, porta/adaptador, eventos de domínio, autenticação ou agregados orquestrados. Uma camada de aplicação dedicada é recomendada na v0.2 para separar regras de domínio, política transacional e transporte HTTP.

## Fluxo de criação e transição

~~~mermaid
sequenceDiagram
    actor U as Usuário
    participant R as React
    participant A as Controller
    participant D as PostgreSQL
    U->>R: Cadastrar workflow
    R->>A: POST /api/v1/workflows
    A->>D: INSERT workflows ACTIVE
    D-->>A: Salvo
    A-->>R: 201 + WorkflowDto
    U->>R: Executar
    R->>A: POST /workflows/{id}/executions
    A->>D: INSERT execution PENDING
    A-->>R: 201 + ExecutionDto
    U->>R: Iniciar
    R->>A: POST /executions/{id}/start
    A->>D: UPDATE status RUNNING, started_at
    A-->>R: 200
    U->>R: Concluir
    R->>A: POST /executions/{id}/complete
    A->>D: UPDATE status COMPLETED, finished_at
    A-->>R: 200
~~~

Os endpoints de transição utilizam métodos com transação. O backend não dispara um processo externo.

## Decisões de desenho

| Decisão | Justificativa | Consequência |
|---|---|---|
| Monólito inicial | Desenvolvimento e implantação simples | Menos complexidade operacional |
| UUID nas entidades | Identificadores desacoplados do banco | Índices maiores que inteiros |
| PostgreSQL + Flyway | Integridade referencial e evolução rastreável | Migrações obrigatórias |
| DTOs na API | Evitar serializar entidades JPA diretamente | Contrato definido no Controller |
| Validação de transição no domínio | Tornar estados inválidos explícitos | Não substitui controle de concorrência |
| CHECK constraints na V2 | Segunda linha de defesa para integridade | Mudança de estado requer atualização de schema |

## Evolução recomendada

1. Extrair WorkflowService / WorkflowExecutionService; centralizar casos de uso e transações.
2. Introduzir enum de status, erros padronizados (Problem Details) e contrato OpenAPI.
3. Acrescentar versão otimista / lock para evitar condições de corrida em transições concorrentes.
4. Implementar autenticação, RBAC e auditoria de ação e responsável.
5. Introduzir jobs assíncronos, fila, idempotency keys, retries limitados, DLT e compensação quando necessário.
6. Só então integrar Spring AI e ferramentas externas com allowlist, cotas e aprovação para ações sensíveis.

Ver [ADR-0001](adr/0001-monolith-first.md) e [Segurança e Qualidade](security-and-quality.md).

