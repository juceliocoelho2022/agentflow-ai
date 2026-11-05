# Catálogo de Regras de Negócio — AgentFlow AI

**Versão de referência:** MVP v0.1 com aprimoramento de integridade temporal na V2.

## Entidades do domínio

- **Workflow:** definição cadastrada de um processo reutilizável.
- **WorkflowExecution:** instância de acompanhamento desse workflow.
- **Estado de workflow:** ACTIVE (único estado criado atualmente).
- **Estado de execução:** PENDING → RUNNING → COMPLETED.

## Regras implementadas

| ID | Regra | Verificação / resultado esperado |
|---|---|---|
| RN-001 | Nome obrigatório, não vazio e comprimento máximo 120 | POST inválido recebe 400 |
| RN-002 | Descrição opcional, até 1.000 caracteres | Entrada longa recebe 400 |
| RN-003 | Workflow novo recebe UUID, ACTIVE e created_at | Persistido pelo JPA |
| RN-004 | Workflow pode possuir 0..N execuções | FK execution.workflow_id |
| RN-005 | Execução só pode referenciar workflow existente | 404 antes da inserção; FK no banco |
| RN-006 | Execução criada com UUID, PENDING e created_at | started_at e finished_at nulos (V2) |
| RN-007 | Somente PENDING pode ser iniciado | PENDING → RUNNING, started_at preenchido |
| RN-008 | Somente RUNNING pode ser concluído | RUNNING → COMPLETED, finished_at preenchido |
| RN-009 | Estado COMPLETED não permite novas transições | Requisição indevida recebe 409 |
| RN-010 | Transição fora da ordem recebe HTTP 409 | Ex.: concluir PENDING ou iniciar RUNNING |
| RN-011 | Workflow/execução inexistente gera HTTP 404 | Busca por UUID |
| RN-012 | Listagem do histórico exige workflow existente | GET histórico de ID inexistente recebe 404 |
| RN-013 | Histórico de execuções retorna mais recentes primeiro | Ordenação por created_at DESC |
| RN-014 | Transições são executadas em transação | Métodos @Transactional |
| RN-015 | Fluxo não executa IA ou efeitos externos | Apenas alteração do registro |
| RN-016 | Identificadores e estados respeitam constraints do banco | PK, FK, NOT NULL e CHECK (V2) |

### Observações importantes

- A API não impede o cadastro de nomes de workflow repetidos. **Unicidade não é uma regra implementada.**
- A API não normaliza nomes com espaços excedentes. @NotBlank impede texto apenas em branco, mas não realiza trim automaticamente.
- Não há exclusão, edição, suspensão ou reativação de workflows.
- Não há endpoints para cancelamento, falha, retry ou reprocessamento.
- Validação de estados em memória **não é garantia de exclusão mútua**. Sem bloqueio otimista/pessimista, chamadas simultâneas podem competir.
- Eventos anteriores à migração V2 não preservam o instante real de início; o instante anteriormente gravado representa a criação.

## Cenários de aceitação (Given / When / Then)

### CA-001 — Criar workflow válido
**Dado** nome não vazio e tamanho permitido, **quando** POST /api/v1/workflows, **então** retorna 201, UUID e status ACTIVE.

### CA-002 — Rejeitar nome inválido
**Dado** nome em branco ou com mais de 120 caracteres, **quando** criar, **então** retorna 400 e nenhum workflow é salvo.

### CA-003 — Criar execução
**Dado** workflow existente, **quando** POST /{id}/executions, **então** retorna 201 e execução PENDING com createdAt preenchido, startedAt/finishedAt nulos.

### CA-004 — Iniciar execução
**Dada** uma execução PENDING, **quando** POST /executions/{id}/start, **então** retorna 200, status RUNNING e startedAt preenchido.

### CA-005 — Concluir execução
**Dada** uma execução RUNNING, **quando** POST /executions/{id}/complete, **então** retorna 200, status COMPLETED e finishedAt preenchido.

### CA-006 — Bloquear transição inválida
**Dada** uma execução PENDING, **quando** tentar concluir diretamente, **então** retorna 409 e estado permanece PENDING.

### CA-007 — Bloquear workflow inexistente
**Dado** UUID sem correspondência, **quando** criar execução ou consultar seu histórico, **então** retorna 404.

### CA-008 — Ordenação
**Dado** um workflow com múltiplas execuções, **quando** listar histórico, **então** o retorno está ordenado pela data de criação descendente.

> Os testes unitários atuais cobrem o ciclo de transição e timestamps. A automação dos cenários HTTP, banco e concorrência exige testes de integração adicionais.

## Regras propostas (NÃO implementadas)

| ID | Proposta | Critério mínimo futuro |
|---|---|---|
| RN-F01 | Autenticar usuário e controlar acesso a workflows | JWT/OIDC e RBAC |
| RN-F02 | Garantir idempotência no disparo de jobs | Chave por solicitação e rejeição de duplicatas |
| RN-F03 | Registrar auditoria de cada decisão | Autor, ação, timestamp e correlação |
| RN-F04 | Submeter ações sensíveis à aprovação humana | Estado WAITING_APPROVAL e prazo |
| RN-F05 | Executar jobs com retry e timeout | Limites, backoff e DLT |
| RN-F06 | Aplicar limites de custo e acesso das ferramentas de IA | Allowlist, orçamento e política de execução |

Não confundir propostas com o estado atual do sistema.

