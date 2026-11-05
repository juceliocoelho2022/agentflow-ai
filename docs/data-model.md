# Modelo de dados e DER — AgentFlow AI

**Banco:** PostgreSQL 17 | **ORM:** Spring Data JPA / Hibernate | **Versionamento:** Flyway.

## DER físico (após migração V2)

~~~mermaid
erDiagram
    WORKFLOWS ||--o{ WORKFLOW_EXECUTIONS : "1:N"
    WORKFLOWS {
        UUID id PK
        VARCHAR_120 name "NOT NULL"
        VARCHAR_1000 description "NULL"
        VARCHAR_30 status "NOT NULL ACTIVE"
        TIMESTAMPTZ created_at "NOT NULL"
    }
    WORKFLOW_EXECUTIONS {
        UUID id PK
        UUID workflow_id FK "NOT NULL"
        VARCHAR_30 status "NOT NULL"
        TIMESTAMPTZ created_at "NOT NULL"
        TIMESTAMPTZ started_at "NULL"
        TIMESTAMPTZ finished_at "NULL"
    }
~~~

**Cardinalidade:** um WORKFLOW pode possuir zero ou muitas WORKFLOW_EXECUTIONS; cada execução pertence obrigatoriamente a exatamente um workflow.

## Dicionário de dados

### workflows

| Coluna | Tipo | Obrigatório | Semântica |
|---|---|---|---|
| id | UUID | Sim, PK | Identificador |
| name | VARCHAR(120) | Sim | Nome do processo |
| description | VARCHAR(1000) | Não | Explicação do processo |
| status | VARCHAR(30) | Sim | ACTIVE no MVP |
| created_at | TIMESTAMPTZ | Sim | Criação do workflow |

### workflow_executions

| Coluna | Tipo | Obrigatório | Semântica |
|---|---|---|---|
| id | UUID | Sim, PK | Identificador |
| workflow_id | UUID | Sim, FK | Workflow proprietário |
| status | VARCHAR(30) | Sim | PENDING, RUNNING ou COMPLETED |
| created_at | TIMESTAMPTZ | Sim | Criação da execução |
| started_at | TIMESTAMPTZ | Não | Início real, após start (V2) |
| finished_at | TIMESTAMPTZ | Não | Conclusão, após complete |

## Constraints e índices

- PRIMARY KEY em ambos os IDs.
- FOREIGN KEY workflow_executions.workflow_id → workflows.id; sem ON DELETE CASCADE explícito.
- CHECK chk_workflow_status: ACTIVE.
- CHECK chk_execution_status: PENDING, RUNNING ou COMPLETED.
- CHECK chk_execution_lifecycle: timestamps compatíveis com cada estado.
- Índice original: idx_execution_workflow (workflow_id).
- Índice V2: idx_execution_workflow_created_at (workflow_id, created_at DESC).

**Nota:** a unicidade dos nomes não é imposta; não há exclusão de registros pela API.

## Evolução Flyway

| Versão | Responsabilidade |
|---|---|
| V1__init.sql | Criação das tabelas, PK/FK e índice por workflow |
| V2__execution_lifecycle.sql | Coluna created_at da execução, correção do ciclo temporal, índices e CHECK |

### Migração de registros existentes

Na V1, started_at era preenchido mesmo em PENDING. A V2:
1. Cria created_at e copia para ele o valor antigo de started_at.
2. Permite started_at nulo e limpa o campo para registros PENDING.
3. Preserva started_at dos registros RUNNING e COMPLETED como informação legada.
4. Instala os constraints e o índice ordenado pela criação.

**Limitação histórica:** para registros que já estavam RUNNING ou COMPLETED antes da V2, started_at não representa necessariamente o início real. Não inferir métricas de duração históricas com esse campo.

## Modelo de evolução futura (conceitual, não implementado)

~~~mermaid
erDiagram
    USERS ||--o{ WORKFLOWS : owns
    WORKFLOWS ||--o{ WORKFLOW_STEPS : defines
    WORKFLOWS ||--o{ WORKFLOW_EXECUTIONS : executes
    WORKFLOW_EXECUTIONS ||--o{ EXECUTION_EVENTS : records
    WORKFLOW_EXECUTIONS ||--o{ HUMAN_APPROVALS : requires
    AGENTS ||--o{ WORKFLOW_STEPS : performs
~~~

As novas entidades são **apenas direcionamento de arquitetura** e dependem de requisitos, análise de riscos e migrações futuras.

