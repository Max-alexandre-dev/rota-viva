# Plano técnico — Spec 001 (proposta)

## Stack definida anteriormente

Vue 3, TypeScript, Vite, Tailwind CSS, Pinia e Vue Router. API central prevista em Spring Boot + PostgreSQL. No protótipo, utilizar uma única fonte de dados sintéticos, sem simular chamadas HTTP como se fossem reais.

## Estrutura sugerida

```text
src/
  app/
    router.ts
  components/
    AppShell.vue
    StatCard.vue
    StatusBadge.vue
    EmptyState.vue
    ErrorState.vue
    TransferSuggestionCard.vue
  views/
    DashboardView.vue
    TransfersView.vue
  stores/
    operations.ts
  services/
    operationsApi.ts       # interface do provider; implementação mock inicial
    operationsMock.ts
  domain/
    types.ts               # já existente: preservar regras de negócio
    logic.ts
```

Não criar tudo se parte já existir; adaptar à estrutura real. Não duplicar cálculo de estoque em componentes.

## Fluxo de dados

`provider (mock/api) → store Pinia → funções de domínio → ViewModels → componentes`.

- O cálculo real de excedentes/déficits fica no domínio/backend, não em cartões visuais.
- Um adaptador mock e outro REST respeitam o **mesmo contrato**; alternar por configuração explícita.
- Sem API disponível, a UI deve indicar `Demonstração — dados fictícios`.
- Deixar ações mutáveis desabilitadas/identificadas se só houver leitura no P0 inicial.

## Testes

- Unidade: ordenação por prioridade, valores de déficit e não inclusão de centros inativos.
- Componente: renderização de lista vazia, erro, status de proposta e navegação.
- Integração: uma necessidade → uma sugestão → abertura de transferência.
- UI: 390/768/1440 px, teclado, contraste e feedback.

## Decisões técnicas pendentes

- Confirmar contratos REST e semântica de `reserved`, `onHand`, `incoming`.
- Resolver P03 contagem física menor que reservas, sem ocultar verdade de estoque.
- Confirmar status de missão e quem registra retirada/recebimento.

## Observação sobre o scaffold existente

O scaffold analisado tem modelos e lógica de domínio TypeScript e um roteador que referencia views. Antes de implementar, verificar a presença real dos componentes Vue, do `App.vue` e de `style.css`; não declarar o projeto executável sem testar `npm run build`.
