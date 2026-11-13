# Prompt inicial para o Codex — SDD/UX

Copie o texto abaixo no chat do Codex no repositório elegível da Equipe 404.

---

Estamos construindo o Rota Viva, aplicação de coordenação logística entre centros de apoio para o Hack Brasil 2026. Queremos aplicar Spec-Driven Development (SDD) com prioridade para UX real, integridade operacional e escopo fechado.

Leia primeiro:
- `docs/CONSTITUTION_DRAFT.md`
- `specs/001-dashboard-operacional/spec.md`
- `specs/001-dashboard-operacional/plan.md`
- `specs/001-dashboard-operacional/tasks.md`
- a implementação existente de `src/domain/`

Objetivo imediato: construir a **primeira fatia vertical do dashboard do coordenador**: mostrar necessidades por prioridade, sugestões de transferência explicadas e permitir navegar para detalhes de transferência.

**Antes de alterar arquivos:**
1. Analise a estrutura real do projeto, aponte arquivos ausentes e inconsistências com a spec.
2. Apresente um plano de tarefas pequenas, com arquivos-alvo e critérios de aceite AC01–AC16.
3. Diga claramente quais regras ou contratos de API ainda precisam de aprovação; não presuma que estão definidos.
4. Não implemente P1, P2, login real, mapas com rotas, scores de eficiência inventados ou comunicação por rádio.
5. Não copie código privado pré-evento. O código produzido deve ser novo e respeitar o regulamento.

**Na implementação após revisão:**
- Vue 3, TypeScript, Vite, Tailwind, Pinia e Vue Router.
- Layout profissional e sóbrio, com foco em legibilidade, responsividade e ações operacionais.
- Mock explícito e intercambiável com REST; nunca apresentar dados simulados como atualizações do servidor.
- Não duplicar regras de saldo e disponibilidade nos componentes visuais.
- Incluir estados de erro, vazio e carregamento; mensagens de conflito; foco e teclado.
- Executar testes de domínio e build; informar resultados reais, não presumidos.
- Encerrar confrontando cada AC com evidência do comportamento implementado.

Comece **somente com auditoria + plano de implementação**; não edite código antes de apresentar o plano.
