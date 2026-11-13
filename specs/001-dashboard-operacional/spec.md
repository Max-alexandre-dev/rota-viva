# Spec 001 — Dashboard operacional do coordenador

**Versão:** 0.1 · **Estado:** proposta, aguardando revisão · **Responsável funcional:** equipe 404

## Problema

Em uma emergência, o coordenador precisa enxergar rapidamente quais centros têm escassez, quais oferecem recursos e o que pode ser transferido. Uma tela cheia de números, sem significado e sem próximas ações, atrapalha.

## Resultado de usuário

Ao abrir a aplicação, o coordenador identifica um déficit relevante, entende a origem de uma sugestão e navega para avaliar sua aprovação. Não pode confundir uma proposta com uma entrega feita.

## Personas operacionais

- **Coordenador:** vê toda a rede, analisa e aprova transferências.
- **Operador de centro:** atualiza apenas dados operacionais de seu centro; não edita nome/status cadastral.
- **Voluntário:** acompanha missão atribuída na etapa seguinte; não necessita painel global no primeiro corte.

> Autenticação/permissões reais ainda dependem do escopo fechado. Na demonstração, não declarar controle de acesso se houver apenas seletor de perfis simulado.

## Fluxo prioritário (cenário sintético)

1. Coordenador acessa a **Visão geral**.
2. Identifica necessidade crítica: Centro B precisa de 150 garrafas de água; possui 20 em estoque e meta 170.
3. Encontra sugestão explicada: Centro A tem estoque físico 500, reserva mínima 200 e 300 unidades inicialmente ofertáveis (sem outras reservas).
4. Abre detalhes da sugestão **A → B, 150 unidades**.
5. Compreende a diferença entre **Sugerida** e **Aprovada**.
6. Navega para **Transferências** e, se a funcionalidade estiver implementada, aprova explicitamente; caso contrário, exibe estado de demonstração claramente identificado.
7. Recebimento e saldo do destino só mudam depois da confirmação de entrega no fluxo completo (feature posterior).

## Componentes da primeira tela

- Cabeçalho com título concreto, informação de cenário de demonstração e estado da conexão.
- Sidebar desktop e navegação mobile.
- Indicadores de centros ativos, recursos com déficit, transferências pendentes e em trânsito, calculados dos dados disponíveis.
- Lista **Necessidades prioritárias** com centro, recurso, quantidade pendente e prioridade.
- Bloco **Sugestões de transferência**, cada uma com origem/destino/recurso/quantidade/justificativa/status e botão `Ver detalhes`.
- Ação principal `Analisar transferências`.
- Feedback de carregamento, vazio, erro de carregamento e conflito.
- Nenhum mapa verdadeiro/rota segura fictícia; se a geografia não fizer parte da API, usar lista de centros.

## Histórias e aceites

### US01 — Saber onde agir

**Como** coordenador, **quero** ver os déficits priorizados **para** decidir qual necessidade exige atenção.

- AC01: lista mostra centro, recurso, déficit e prioridade, com linguagem clara.
- AC02: ordenação explicada (alta antes da média antes da baixa; empate determinístico).
- AC03: se não houver déficit, mostrar estado vazio útil, não tabela em branco.
- AC04: valores são originados de dados sintéticos rotulados ou de API; não usar KPI inventado.

### US02 — Entender sugestões

**Como** coordenador, **quero** entender por que uma transferência foi sugerida **para** poder tomar decisão informada.

- AC05: proposta mostra origem, destino, recurso, quantidade, saldo ofertável e justificativa.
- AC06: propostas têm rótulo `Sugerida` e não alteram saldos por si.
- AC07: botão de detalhes funciona ou, se não disponível, não é apresentado como funcional.
- AC08: não sugerir origem inativa nem quantidade acima da disponibilidade ofertável.

### US03 — Navegar sem perder contexto

**Como** coordenador, **quero** sair do dashboard para a lista de transferências sem perder a noção da necessidade.

- AC09: ação `Analisar transferências` navega à rota `/transferencias`.
- AC10: item sugerido pode abrir a tela de detalhes ou filtragem correspondente, se implementados.
- AC11: indicação de status é textual, não apenas por cores.
- AC12: layout fica legível em desktop (>=1280 px) e mobile (360–390 px), sem rolagem horizontal involuntária.

### US04 — Entender falhas e incertezas

- AC13: erro de rede exibe mensagem e ação `Tentar novamente` se a consulta remota existir.
- AC14: conexão indisponível não é confundida com operação offline sincronizada; exibir aviso de possível desatualização.
- AC15: ações que alterem dados exigem confirmação e feedback de sucesso/erro; erros 409 são apresentados como conflito a revisar.
- AC16: teclado, foco visível e rótulos acessíveis funcionam nas ações importantes.

## Fora do escopo desta feature

- Mapa geográfico interativo e roteamento de transporte.
- Rastreamento ao vivo de voluntários, cálculo de eficiência sem fonte e estatísticas de operações fictícias.
- Motor de sincronização offline, rádio, integração satelital.
- Cadastro completo e autenticação real, se ainda não aprovados no contrato.

## Indicadores de qualidade (metas de teste, não fatos)

- Em teste com roteiro, usuário encontra necessidade crítica e chega à análise de transferência sem orientação.
- Dados e status exibidos batem com cenário sintético e lógica de domínio.
- Nenhum controle visível promete ação indisponível.
- Revisão em teclado, 390px e 1440px concluída.

## Pendências a validar

- Confirmação dos perfis reais e autenticação do MVP.
- Contrato REST para `needs` e `transfer proposals`.
- Regra de conflito físico abaixo de reserva e feedback da UI.
- Se haverá painel de histórico na primeira sprint ou somente ligação para a tela existente.
