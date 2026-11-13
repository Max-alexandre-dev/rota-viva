# Rota Viva

**Logística humanitária inteligente para conectar centros de apoio, identificar necessidades e coordenar a redistribuição de recursos.**

> **Equipe 404 · Hack Brasil 2026 · Trilha 07 — Logística & Automação**  
> **Status:** protótipo em desenvolvimento. Esta versão é demonstrativa e utiliza **dados sintéticos**.

## O problema

Em emergências, diferentes centros de apoio têm estoques, necessidades e reservas mínimas distintas. Sem uma visão integrada, recursos podem ficar parados em um centro enquanto faltam em outro. O Rota Viva propõe uma visão compartilhada para apoiar a coordenação humana das transferências.

## Nossa solução

O sistema permite:

1. **Gerenciar centros e recursos:** cadastrar centros, manter saldos de estoque e registrar necessidades e reservas mínimas.
2. **Identificar déficits e excedentes:** cruzar informações entre centros e explicar quais recursos podem ser transferidos.
3. **Coordenar transferências:** gerar propostas para aprovação humana e, nas próximas entregas, acompanhar retirada e recebimento.

**Evolução planejada:** associação de voluntários e missões de transporte; modo offline parcial com sincronização segura, se houver tempo após o fluxo principal.

### Como a lógica funciona (cenário demonstrativo)

- Centro A possui 500 garrafas de água e deve preservar 200 → pode ofertar até 300.
- Centro B possui 20 e precisa de 170 → déficit de 150.
- Centro C possui 50 e precisa de 150 → déficit de 100.
- O algoritmo apresenta propostas **A → B (150)** e **A → C (100)**, sem tratar proposta como entrega confirmada.

> **Importante:** o software **não** avalia se uma rota é segura, não dispensa aprovação de um coordenador e não está homologado para uso em emergências reais.

## Tecnologias

| Camada | Stack proposta |
|---|---|
| Frontend | Vue 3 · TypeScript · Vite · Pinia · Vue Router · Tailwind CSS |
| Backend | Java 21 · Spring Boot · API REST |
| Persistência planejada | PostgreSQL |
| Gestão do desenvolvimento | Spec-Driven Development (SDD) · GitHub Issues · Pull Requests |

O frontend incluído é um **protótipo de consulta com dados sintéticos locais**. O backend incluído inicia com um endpoint de saúde; **a integração real entre as camadas ainda está pendente**.

## Estrutura do repositório

```text
rota-viva/
├── frontend/                 # Vue 3, componentes, domínio demonstrativo e testes
│   └── src/
├── backend/                  # Spring Boot / API REST (estrutura inicial)
│   └── src/
├── docs/                     # Produto, especificação, regras e API (propostas)
├── specs/                    # SDD: especificação, plano e tarefas
├── .github/                  # Templates de Pull Request e Issues
├── .gitignore
└── README.md
```

## Como executar

**Frontend** (Node.js LTS):

```bash
cd frontend
npm install
npm run dev
```

**Backend** (JDK 21 e Maven):

```bash
cd backend
mvn spring-boot:run
```

Endpoint inicial: `http://localhost:8080/api/health`.

Atenção: o frontend **não** está consumindo o backend ainda. Nenhum dado sintético deve ser confundido com uma operação real.

## Documentação e decisões

- [Escopo funcional e regras em discussão](docs/especificacao-mvp-proposta.md)
- [Requisitos e visão de produto](docs/visao-produto.md)
- [Contrato REST em elaboração](docs/api-contract.md)
- [Decisões que dependem de validação](docs/decisoes-pendentes.md)
- [Diretrizes SDD e experiência de usuário](docs/SDD_WORKFLOW.md)
- [Primeira especificação de UX](specs/001-dashboard-operacional/spec.md)

As funções e decisões técnicas ainda não aprovadas pelos três integrantes estão marcadas como **propostas**. O escopo será fechado e atualizado no GitHub em conjunto.

## Colaboração e segurança

- Fluxo recomendado: `main` estável → `feature/<tema>` → Pull Request → revisão de outro integrante.
- Não subir `.env` contendo segredos, senhas, dados pessoais de terceiros ou dados operacionais reais.
- Usar dados fictícios nas demonstrações.
- Não registrar entregas como concluídas antes da confirmação pelo destino.
- Reutilização de código e histórico devem respeitar o regulamento da competição.

## Hack Brasil 2026

Prazo de submissão: **30/11/2026 às 23h59 (Brasília)**. A equipe deve entregar repositório público, demonstração online e vídeo de até três minutos. Os repositórios submetidos devem ter sido **criados após 04/11/2026 às 21h**, sem histórico anterior importado, e permanecer públicos pelo menos até **15/12/2026**.

Regulamento: https://hackbrasil.com/regulamento

## Equipe

**Equipe 404 — três desenvolvedores.** Distribuição de responsabilidade e contribuições a definir coletivamente. Direitos e utilização futura do projeto serão acordados pelo time.
