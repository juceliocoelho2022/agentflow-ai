# Segurança, qualidade e requisitos não funcionais

## Classificação de maturidade

**Uso recomendado:** desenvolvimento local e demonstração controlada. **Não pronto para produção** sem implementar os controles abaixo.

## Riscos presentes no MVP

| Área | Situação atual | Risco | Ação recomendada |
|---|---|---|---|
| Autenticação | Ausente | Acesso irrestrito a endpoints | JWT/OIDC |
| Autorização | Ausente | Sem isolamento de usuários | RBAC / ownership |
| Credenciais | Fixas para desenvolvimento no Compose | Exposição se reutilizadas | Variáveis/secret manager |
| Rede | Porta PostgreSQL publicada no host | Maior superfície de ataque | Bind local/segmentação |
| Auditabilidade | Sem log de responsável por transição | Baixa rastreabilidade | Audit events |
| Concorrência | Sem @Version ou lock de transições | Race conditions | Lock otimista e HTTP 409 |
| Abuso de API | Sem rate limiting | DoS e abuso | Quotas e limites |
| IA | Não implementada | Sem risco atual de execução de ferramentas IA | Allowlist, autorização, custos e aprovação futura |
| Banco | Volume local | Perda de dados sem backup | Política de backup/restauração |
| Monitoramento | Health/info | Sem telemetria de execução | Logs estruturados, métricas e traces |

As credenciais em compose.yaml e os acessos por HTTP devem ser tratados apenas como configuração de desenvolvimento. Não expor serviços sem TLS, autenticação, autorização e rede controlada.

## Estratégia de testes

| Nível | Escopo | Situação |
|---|---|---|
| Teste unitário | Estados e timestamps da entidade WorkflowExecution | Incluído |
| Build Maven | Compilação e JUnit | Configurado no CI |
| Build frontend | TypeScript e Vite | Configurado no CI |
| Integração | HTTP + Flyway + PostgreSQL/Testcontainers | Pendente |
| Contrato | Schemas e erros da API | Pendente |
| E2E | Cadastro → criação → início → conclusão pelo navegador | Pendente |
| Segurança | SAST/SCA, secrets scanning, autorização | Pendente |
| Resiliência | Concorrência, falhas, retries, idempotência | Pendente |

**Critérios propostos para PRs futuros:** build verde, novos casos com testes, migrations reversíveis ou com estratégia de recuperação, mudanças de API documentadas, revisão de riscos e ausência de credenciais novas no commit.

## CI já configurado nesta branch

~~~mermaid
flowchart LR
    PR[Pull Request] --> B[Java 21 - Maven verify]
    PR --> F[Node 22 - npm run build]
    B --> R[Revisão / aprovação]
    F --> R
~~~

Este diagrama descreve o pipeline **definido**, não comprova execução bem-sucedida. Verifique a aba Actions.

## Metas não funcionais sugeridas (não são SLOs existentes)

- **Disponibilidade:** estabelecer SLO e monitoramento antes de produção.
- **Observabilidade:** correlação por request ID, logs estruturados e métricas de latência/erro.
- **Segurança:** HTTPS, OAuth2/OIDC, RBAC, princípio do menor privilégio.
- **Escalabilidade:** paginação e limites de tamanho; avaliar filas e workers quando houver execução externa.
- **Confiabilidade:** idempotência, timeout, retry com backoff, tratamento de mensagens duplicadas.
- **Persistência:** backup agendado, restauração testada, migrações compatíveis.
- **Desempenho:** definir p95/p99 mediante medições reais, não números arbitrários.

## Checklist antes de publicar na internet

- [ ] Substituir credenciais de desenvolvimento e proteger segredos.
- [ ] Proteger API com autenticação e autorização por recurso.
- [ ] Restringir o acesso ao banco e habilitar TLS.
- [ ] Implementar validação de propriedade e evitar IDOR.
- [ ] Tratar concorrência e idempotência em transições.
- [ ] Criar testes de integração e auditoria.
- [ ] Aplicar rate limits e controles de CORS/CSRF conforme a arquitetura.
- [ ] Configurar backups, alertas e plano de recuperação.
- [ ] Criar política de retenção e privacidade de dados.
- [ ] Revisar governança e custo das ferramentas de IA antes de habilitá-las.

