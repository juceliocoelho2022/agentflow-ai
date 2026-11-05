# API REST — Contrato observado no código

**Base URL local:** http://localhost:8088

**Content-Type:** application/json | **Versão:** /api/v1

A implementação utiliza Spring Web + DTOs Java record. **Ainda não existe especificação OpenAPI publicada nem padronização customizada de erros.**

## Endpoints

| Método | Endpoint | Status principal | Erros previsíveis |
|---|---|---|---|
| GET | /api/v1/workflows | 200 | — |
| POST | /api/v1/workflows | 201 | 400 |
| GET | /api/v1/workflows/{workflowId}/executions | 200 | 404 |
| POST | /api/v1/workflows/{workflowId}/executions | 201 | 404 |
| POST | /api/v1/workflows/executions/{executionId}/start | 200 | 404, 409 |
| POST | /api/v1/workflows/executions/{executionId}/complete | 200 | 404, 409 |
| GET | /actuator/health | 200 quando saudável | Depende da condição da aplicação |

## Modelos de requisição/resposta

### CreateWorkflow (entrada POST)

~~~json
{
  "name": "Gerenciamento de tarefas",
  "description": "Exemplo de workflow"
}
~~~

name é obrigatório e limitado a 120 caracteres. description é opcional, até 1.000.

### WorkflowDto (resposta)

~~~json
{
  "id": "a90c9812-8c04-4b5c-aed4-43268b11d5b6",
  "name": "Gerenciamento de tarefas",
  "description": "Exemplo de workflow",
  "status": "ACTIVE"
}
~~~

### ExecutionDto (resposta após criação, V2)

~~~json
{
  "id": "3dc1d6f8-8f89-47a2-94bc-981b38b91955",
  "workflowId": "a90c9812-8c04-4b5c-aed4-43268b11d5b6",
  "status": "PENDING",
  "createdAt": "2026-11-04T18:00:00Z",
  "startedAt": null,
  "finishedAt": null
}
~~~

Os UUIDs e datas acima são **exemplos ilustrativos**. Após iniciar, status vira RUNNING e startedAt é preenchido; após concluir, COMPLETED e finishedAt é preenchido.

## Teste manual no PowerShell

~~~powershell
$base = "http://localhost:8088/api/v1/workflows"

# 1. Criar workflow
$workflow = Invoke-RestMethod -Method Post -Uri $base -ContentType "application/json" -Body (@{
    name = "Processamento demonstrativo"
    description = "Validação manual do ciclo"
} | ConvertTo-Json)

# 2. Criar execução PENDING
$execution = Invoke-RestMethod -Method Post -Uri "$base/$($workflow.id)/executions"

# 3. Iniciar
Invoke-RestMethod -Method Post -Uri "$base/executions/$($execution.id)/start"

# 4. Concluir
Invoke-RestMethod -Method Post -Uri "$base/executions/$($execution.id)/complete"

# 5. Listar histórico (mais recente primeiro)
Invoke-RestMethod -Method Get -Uri "$base/$($workflow.id)/executions"
~~~

## Testes de erro

- Criar workflow com nome vazio: HTTP 400.
- Criar execução para UUID inexistente: HTTP 404.
- Concluir execução PENDING diretamente: HTTP 409.
- Iniciar execução já COMPLETED: HTTP 409.

O formato exato dos erros segue as respostas do Spring Boot no momento; não dependa de propriedades não versionadas em clientes externos.

## Aspectos que exigem evolução

- Paginação e filtros para evitar listas ilimitadas;
- OpenAPI/Swagger como contrato verificável;
- DTOs e erros padronizados via RFC 9457 Problem Details;
- Autenticação, autorização, rate limits e idempotência;
- Headers de correlação, métricas e versionamento de contrato;
- Testes de integração com PostgreSQL real via Testcontainers.

