# SDD para a Equipe 404: workflow leve

## Por quê

SDD é **Spec-Driven Development**, não uma biblioteca de UI. Ele organiza a construção em especificação de comportamento → decisões de UX → plano técnico → tarefas → implementação → verificação.

A Equipe 404 tem pouco tempo até 30/11/2026; usar SDD em **fatias verticais pequenas** e reuniões de aprovação curtas.

## Integração oficial opcional: GitHub Spec Kit + Codex

Referência: https://github.github.com/spec-kit/quickstart.html e https://github.github.com/spec-kit/integrations/codex.html

1. Fazer commit/backup do projeto **elegível** atual antes de instalar ferramentas que criam arquivos.
2. Instalar `uv` e Codex CLI, se ainda não disponíveis.
3. No terminal, dentro do projeto: `uv tool install specify-cli` (conforme documentação oficial).
4. Inicializar a integração: `specify init --here --integration codex` e **revisar** arquivos adicionados/alterados. Em projeto não vazio pode haver aviso de merge.
5. No chat do Codex, chamar os skills pela sintaxe documentada: `$speckit-constitution`, `$speckit-specify`, `$speckit-plan`, `$speckit-tasks`, `$speckit-implement`, `$speckit-converge`. A depender do modo/versão, consultar a lista instalada de skills.
6. Usar os arquivos deste kit como insumos; a configuração oficial pode criar suas próprias estruturas. Não sobrescrever arquivos já revisados sem comparar.

## Nosso ciclo mínimo

1. **Especificar**: escolher uma ação real do usuário e testar fluxo principal, erro e vazio.
2. **Validar**: Dev 1 confirma regra de domínio e Dev 2 confirma UX, Dev 3 identifica integrações.
3. **Planejar**: componentes, store, adaptador da API, critérios de teste e estimativa.
4. **Implementar**: apenas as tarefas dessa feature, uma por PR pequeno.
5. **Convergir**: comparar código com ACs e conferir no navegador/testes.

## Guardrails do hackathon

- Não afirmar que o projeto é offline se apenas usa localStorage/IndexedDB sem política de conflito.
- Não usar mapas fictícios para dar a impressão de roteirização real.
- Não criar telas sem ação: se card for clicável, deve abrir detalhes concretos.
- Não fazer backend e frontend divergirem em status, quantidades, prioridades e reservas.
- Não substituir validação humana por algoritmo em assuntos de segurança de transporte.
