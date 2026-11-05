# ADR-0001 — Começar com backend monolítico e dados relacionais

- **Status:** Aceita para o MVP
- **Data:** 2026-11-04
- **Contexto:** AgentFlow AI precisa estabelecer domínio, estados e persistência antes de executar automações e agentes.

## Decisão

Manter inicialmente um backend Java 21 / Spring Boot, API REST, Spring Data JPA e PostgreSQL com schema controlado pelo Flyway. Uma interface React utiliza o backend via proxy Nginx no Docker Compose.

## Justificativas

1. Menor custo de desenvolvimento e operação.
2. Regras de negócio podem ser verificadas com testes unitários antes de distribuir responsabilidades.
3. Banco relacional facilita integridade, histórico e consistência de transições.
4. Migrações versionadas permitem evolução incremental sem substituir a base existente.

## Consequências

- **Positivas:** configuração simples, depuração concentrada, menor complexidade operacional.
- **Limitações:** o controller ainda coordena persistência e erros HTTP; não há isolamento por serviço nem processos em background; concorrência requer proteção adicional.
- **Mitigações futuras:** extrair camada de aplicação, modularizar domínios, incluir eventos e jobs assíncronos apenas com necessidade demonstrada.

## Alternativas consideradas

- Microsserviços desde o primeiro MVP: rejeitado por complexidade prematura.
- Solução apenas em memória: rejeitada por não preservar registros.
- Agentes de IA como primeira funcionalidade: adiado até existirem controles de identidade, autorização, orçamento, auditoria e aprovação para ações sensíveis.

## Regra para revisitar

Revisar esta decisão quando houver necessidades comprovadas de execução assíncrona, escalabilidade independente, isolamento de falhas ou governança de agentes.

