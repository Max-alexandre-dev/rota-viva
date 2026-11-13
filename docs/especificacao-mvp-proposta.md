# Rota Viva — Especificação funcional e divisão de desenvolvimento

> **Equipe 404 · Hack Brasil 2026 · Trilha 07: Logística e Automação**  
> **Versão:** 0.1 — proposta para discussão  
> **Data-base:** 09/11/2026  
> **Prazo de submissão:** 30/11/2026, às 23h59 (Brasília)  
> **Situação:** direção do produto definida na conversa; escopo detalhado, responsáveis e decisões técnicas **dependem de aprovação dos três integrantes**.

## 1. Resumo executivo

**O Rota Viva coordena a redistribuição de recursos entre centros de apoio em emergências.** Ele conecta três etapas: (1) gestão de recursos e necessidades, (2) planejamento e aprovação de transferências, (3) gestão de voluntários e entregas. A demonstração deve percorrer uma necessidade até o recebimento, com alterações de estoque e histórico auditável.

**Limite de escopo:** um sistema de apoio à decisão, **não** um despachante autônomo, sistema de navegação segura ou solução homologada para resposta a desastres reais. Na demo, utilizar apenas centros, voluntários e dados **sintéticos**.

**Diferencial do MVP:** fazer os três pilares conversarem com regras transparentes: identificar faltas e excedentes, sugerir uma transferência sem comprometer reservas, atribuir uma missão compatível a voluntário disponível e registrar retirada/recebimento sem duplicar estoque.

## 2. Escopo por prioridade

### P0 — precisa estar funcional até a submissão

| ID | Pilar | Funcionalidade | Resultado esperado |
|---|---|---|---|
| RF01 | Recursos | Cadastrar/listar centros de apoio | Identificador, nome, região/localização textual |
| RF02 | Recursos | Cadastrar/listar tipos de recursos | Nome, unidade e peso estimado por unidade para checar capacidade |
| RF03 | Recursos | Registrar entrada/correção de estoque com motivo | Saldo e histórico consistentes |
| RF04 | Recursos | Registrar meta de atendimento e reserva mínima por centro/recurso | Dados usados nas sugestões de redistribuição |
| RF05 | Transferências | Calcular excedentes e déficits | Indicadores explicáveis e reproduzíveis |
| RF06 | Transferências | Gerar sugestões de transferência | Origem, destino, recurso, quantidade e justificativa |
| RF07 | Transferências | Aprovar/rejeitar sugestões | Aprovação reserva saldo de maneira atômica; rejeição não altera saldo |
| RF08 | Voluntários | Cadastrar/listar voluntário de demonstração | Apelido, modalidade (bicicleta/moto/carro), capacidade aproximada em kg e disponibilidade |
| RF09 | Voluntários | Oferecer/atribuir missão vinculada a transferência aprovada | Apenas voluntários disponíveis e com capacidade; confirmação de aceite |
| RF10 | Entregas | Registrar retirada e confirmar recebimento | Baixa da origem na retirada; entrada no destino só no recebimento |
| RF11 | Transversal | Dashboard e histórico da operação | Estoques, déficits, transferências e missões com status |
| RF12 | Transversal | Qualidade mínima e demo publicada | Testes, ambiente estável, README, seed sintético e vídeo |

**Simplificações P0 deliberadas:**
- Cada transferência envolve **um recurso, uma origem e um destino**.
- Cada transferência aprovada pode ser transportada por **uma missão e um voluntário**; repartição automática em múltiplas viagens fica fora do P0. Caso necessário, o coordenador cria transferências menores.
- O algoritmo **sugere** transferências, mas não declara rotas seguras nem dispensa aprovação humana.
- Capacidade do voluntário é filtrada por **peso estimado**; volume, tipo de embalagem, acondicionamento e condições de percurso exigem verificação humana.
- Prioridade é um valor **manual** (por exemplo ALTA, MEDIA, BAIXA); o MVP exibe a prioridade e pode ordenar sugestões, sem tomar decisões humanitárias sensíveis automaticamente.
- Sem pagamento, chat, mapa, localização em tempo real, multi-organizações ou cadastro público de pessoas reais.
- Não construir autenticação/permissões complexas; proteger a demo pública de alterações arbitrárias e deixar explícito que o produto não está pronto para uso operacional real.

### P1 — opcional, somente depois do fluxo P0 integrado

1. PWA instalável e consulta a dados já carregados sem internet.
2. Fila local em IndexedDB para registrar **intenção de recebimento** offline; sincronização posterior identificada por `operationId` único.
3. Tratamento visível de conflitos: operação pendente, enviada, confirmada ou rejeitada para revisão.
4. Sugestões de divisão de carga em várias missões; filtros de disponibilidade mais detalhados.
5. Indicadores de tempo e percentual de atendimento no **cenário simulado**.

### P2 — fora desta competição

Rádio/LoRa real, gateways, roteirização em tempo real, previsão de eventos climáticos, mapas de segurança, GPS permanente, entrega parcial/devolução complexa, fiscalização de identidade, operação multi-ONG e qualquer uso real em emergência sem validações de segurança e privacidade.

## 3. Perfis operacionais da demonstração

| Perfil lógico | Ações na demo | Observações |
|---|---|---|
| Coordenador | Avalia sugestões, aprova/rejeita transferências, atribui missões e acompanha status | Todas as decisões críticas passam por pessoa |
| Operador de centro | Registra estoque/necessidade e confirma retirada ou recebimento do seu centro | Ações devem aparecer no histórico |
| Voluntário | Informa disponibilidade, aceita/recusa missão e sinaliza conclusão da etapa de transporte | O recebimento definitivo é confirmado pelo centro, não apenas pelo voluntário |

**Implementação de acesso para o MVP:** podem existir identidades de demonstração com dados fictícios e controles básicos; o regulamento não exige autenticação, mas uma demo pública com endpoints de escrita precisa de proteção proporcional. **Não usar pessoas reais na demonstração.**

## 4. Divisão sugerida entre os três desenvolvedores

> Os identificadores Dev 1/2/3 são **papéis propostos**, não nomes atribuídos. A equipe deve escolher os responsáveis na reunião.

### DEV 1 — Backend, dados e motor de redistribuição

**Missão:** garantir integridade de estoques e transferências.

**Responsabilidade principal / arquivos:** `backend/` — `centers`, `resources`, `inventory`, `needs`, `transfers` e interfaces de serviço usadas pelo Dev 3.

**Entregas P0:**
1. Estruturar API Spring Boot (Java 21) e conexão PostgreSQL; migrations versionadas.
2. Implementar cadastro/consulta de centros e recursos.
3. Implementar `stock_balance` e `stock_movements`, com entradas e ajustes rastreáveis.
4. Implementar metas, reservas mínimas e cálculo de excedentes/déficits.
5. Implementar sugestões determinísticas de transferência, incluindo justificativa.
6. Implementar criação de proposta, aprovação/rejeição e reserva atômica.
7. Expor serviço transacional `TransferExecutionService` para que o módulo de missões registre retirada e recebimento **sem editar saldos diretamente**.
8. Testar invariantes, concorrência e reenvio de comandos.

**Contrato interno (entregar ao Dev 3 até 12/11):**
- `approveTransfer(transferId, operationId)` → valida e reserva;
- `rejectTransfer(transferId, reason)` → rejeita sem reservar;
- `pickupTransfer(transferId, operationId)` → desconta na origem e libera reserva;
- `receiveTransfer(transferId, operationId)` → credita no destino uma única vez;
- `cancelBeforePickup(transferId, reason)` → devolve reserva ainda não retirada.

**Não é responsabilidade do Dev 1:** telas, cadastro/atribuição de voluntários, pipelines e PWA.

**Critérios de aceite próprios:** nenhum estoque negativo; nenhuma aprovação compromete reserva mínima; operações transacionais; segunda confirmação da mesma operação não duplica lançamentos; testes automatizados cobrindo casos normais e concorrentes.

### DEV 2 — Frontend, UX e fluxo completo da demonstração

**Missão:** tornar os três pilares compreensíveis e operáveis em poucos cliques.

**Responsabilidade principal / arquivos:** `frontend/` — Vue 3 + TypeScript + Vite + Tailwind + Pinia; componentes, rotas, serviços da API e fluxo de demo.

**Entregas P0:**
1. Layout base responsivo com menu e estados de carregamento/erro.
2. Página **Centros e recursos** (cadastro e consulta).
3. Página **Estoque e necessidades** (registro de entrada, saldo, reserva mínima e meta).
4. Dashboard com totais, déficits e transferências pendentes.
5. Página **Sugestões de redistribuição**, com origem, destino, quantidade e justificativa.
6. Ações de criar proposta, aprovar/rejeitar, cancelar antes da retirada e mostrar reserva.
7. Página **Voluntários**, incluindo modalidade, capacidade aproximada e disponibilidade.
8. Página **Missões**, com oferta/aceite, retirada e recebimento confirmado.
9. Histórico cronológico de alterações da transferência/entrega.
10. Cenário de demonstração guiado a partir do seed sintético.

**Regras de UX:** distinguir claramente `proposto`, `reservado`, `em trânsito`, `recebido` e `cancelado`; não mostrar déficit como atendido antes da entrega confirmada; não indicar trajeto seguro; não ocultar erros de API.

**Não é responsabilidade do Dev 2:** modificar regras de estoque no frontend, criar estados globais divergentes do servidor ou implementar backend.

**Critérios de aceite próprios:** fluxo de ponta a ponta disponível em desktop e mobile; textos e ações consistentes com o contrato REST; mensagens claras para 400/409 e falhas de rede; nenhuma atualização de saldo apenas visual sem resposta do servidor.

### DEV 3 — Voluntários, missões, integração e qualidade

**Missão:** ligar o planejamento de transferências à execução do transporte e manter a demo publicável.

**Responsabilidade principal / arquivos:** `backend/` — `volunteers`, `missions`; `docs/`, `.github/workflows/`, configuração de deploy e testes de integração. PWA offline somente se P1 for liberado.

**Entregas P0:**
1. Implementar voluntários fictícios e modalidades (BIKE, MOTO, CAR) com capacidade em kg e disponibilidade.
2. Implementar filtro simples de candidatos: disponível e capacidade suficiente para a carga estimada.
3. Implementar missão vinculada a **uma transferência aprovada**.
4. Implementar oferta/aceite/recusa, atribuição e registro de retirada.
5. Implementar confirmação de recebimento pelo operador de destino.
6. Chamar `TransferExecutionService` do Dev 1 para cada movimento de estoque; nunca fazer `UPDATE stock_balance` fora do serviço de domínio.
7. Criar `docker-compose` local, build/testes automatizados em CI, configuração de deploy e README de execução.
8. Criar seed sintético e roteiro de smoke test antes de cada integração.
9. Organizar testes de API, integração e fluxo E2E com o Dev 2.

**Critérios de aceite próprios:** não vincular missão a transferência não aprovada; não ofertar carga acima da capacidade cadastrada; recusa libera a missão para nova atribuição sem perder a transferência; retirada e recebimento têm rastreabilidade; CI executa com sucesso; demo permanece acessível.

**Atenção de coordenação:** Dev 1 é o único dono das regras de estoque. Dev 3 é o dono das regras de voluntários/missões; ambos combinam interfaces e mudanças de schema em PRs separados. O responsável final pela integração geral será escolhido pela equipe.

## 5. Regra de negócio — estoques e transferências

### 5.1 Conceitos

- `onHand`: quantidade fisicamente registrada no centro.
- `reserved`: quantidade comprometida em transferências aprovadas **ainda não retiradas**.
- `minimumReserve`: quantidade que o centro deve preservar para uso próprio.
- `target`: quantidade desejada no destino.
- `committedIncoming`: quantidade aprovada ou em trânsito para o destino **ainda não recebida**.

**Equações de domínio (por recurso e centro):**

```text
ofertavel = max(0, onHand - reserved - minimumReserve)
deficitFisico = max(0, target - onHand)
deficitPlanejavel = max(0, target - onHand - committedIncoming)
```

- Exibir `deficitFisico` separadamente de `deficitPlanejavel`: promessa ou entrega em trânsito **não equivale** a estoque já recebido.
- A geração de sugestões considera compromissos existentes para evitar oferecer novamente quantidades destinadas a outro centro.
- Sugestão não compromete recursos. Na aprovação, recalcular disponibilidade **dentro da transação** e então reservar.
- Empates de proposta devem ser determinísticos (ex.: prioridade manual, depois identificador), e a justificativa precisa dizer quais limites foram respeitados.

### 5.2 Máquina de estados

```text
TRANSFERÊNCIA
PROPOSTA → APROVADA → EM_TRANSITO → RECEBIDA
    └────→ REJEITADA
              
APROVADA → CANCELADA (somente antes da retirada; libera reserva)

MISSÃO (vinculada à transferência APROVADA)
OFERTADA → ACEITA → RETIRADA → CONCLUIDA
     └──→ RECUSADA (pode ser novamente ofertada a outra pessoa)
```

**Transições com impacto no estoque:**

| Evento | Origem `onHand` | Origem `reserved` | Destino `onHand` |
|---|---:|---:|---:|
| Proposta criada | sem mudança | sem mudança | sem mudança |
| Aprovada | sem mudança | `+qtd` | sem mudança |
| Retirada | `-qtd` | `-qtd` | sem mudança |
| Recebimento confirmado | sem mudança | sem mudança | `+qtd` |
| Cancelada antes da retirada | sem mudança | `-qtd` | sem mudança |

A reserva não altera o estoque físico. A retirada não credita o destino antecipadamente. Após a retirada, cancelamentos/incidentes exigem tratamento específico, **fora do fluxo de cancelamento simples**.

### 5.3 Invariantes obrigatórios

1. Quantidades positivas; sem saldo físico ou reservado negativo.
2. `reserved <= onHand` e `onHand - reserved >= minimumReserve` para compromissos novos.
3. Uma transferência só é aprovada se o destino ainda apresentar déficit planejável suficiente.
4. Uma missão só é criada para transferência aprovada; uma missão ativa por transferência no MVP.
5. A capacidade em kg do voluntário deve ser >= `quantidade × pesoEstimadoPorUnidade`, com revisão manual de outras restrições.
6. Nenhuma movimentação é aplicada duas vezes para o mesmo `operationId`; reenvio idempotente retorna estado já confirmado.
7. Bloqueio de linha / controle de versão e transação no servidor impedem aprovação concorrente além do disponível.
8. Confirmação de recebimento deve corresponder à transferência retirada e ao centro de destino.
9. Uma fila offline, se criada, é apenas um **registro local pendente**, não confirmação global.
10. Não afirmar que rotas são seguras ou que o sistema substitui autoridades e coordenadores.

## 6. Contrato REST mínimo (proposta de versionamento `/api/v1`)

> O contrato deve ser congelado **até 11/11** e documentado em `docs/api-contract.md`. Os caminhos abaixo são uma proposta inicial para discussão; alterações exigem alinhamento entre os três devs.

| Método | Endpoint | Ação | Dono |
|---|---|---|---|
| GET / POST | `/centers` | Listar/criar centros | Dev 1 |
| PATCH | `/centers/{id}` | Editar cadastro do centro | Dev 1 |
| GET / POST | `/resources` | Listar/criar recursos | Dev 1 |
| PATCH | `/resources/{id}` | Editar dados do recurso, com validação | Dev 1 |
| GET | `/inventory?centerId=...` | Saldo e reserva por centro | Dev 1 |
| POST | `/stock-movements` | Entrada/ajuste autorizado | Dev 1 |
| GET / POST | `/needs` | Listar/criar necessidades e reservas mínimas | Dev 1 |
| PATCH | `/needs/{id}` | Atualizar meta, prioridade ou reserva mínima | Dev 1 |
| POST | `/transfer-suggestions` | Calcular sugestões determinísticas | Dev 1 |
| GET / POST | `/transfers` | Listar/criar proposta de transferência | Dev 1 |
| POST | `/transfers/{id}/approve` | Aprovar e reservar | Dev 1 |
| POST | `/transfers/{id}/reject` | Rejeitar proposta | Dev 1 |
| POST | `/transfers/{id}/cancel` | Cancelar antes da retirada e liberar reserva | Dev 1 |
| GET / POST | `/volunteers` | Listar/cadastrar voluntários fictícios | Dev 3 |
| PATCH | `/volunteers/{id}/availability` | Definir disponibilidade | Dev 3 |
| GET | `/transfers/{id}/eligible-volunteers` | Listar candidatos compatíveis | Dev 3 |
| GET / POST | `/missions` | Listar/criar missão/oferta | Dev 3 |
| POST | `/missions/{id}/accept` | Voluntário aceita | Dev 3 |
| POST | `/missions/{id}/decline` | Voluntário recusa | Dev 3 |
| POST | `/missions/{id}/pickup` | Retirada + transação do Dev 1 | Dev 3 |
| POST | `/missions/{id}/receive` | Recebimento + transação do Dev 1 | Dev 3 |
| GET | `/dashboard/summary` | Indicadores consolidados | Dev 1 (contrato com Dev 2) |
| GET | `/transfers/{id}/history` | Histórico de estados e movimentos | Dev 1/3 (contrato definido antes) |

**Formato comum:** JSON, IDs UUID, timestamps ISO-8601 UTC, erros padronizados (`code`, `message`, `details`, `timestamp`), status `409 Conflict` para violação de estoque, versão ou estado. Operações críticas recebem `operationId` gerado no cliente ou `Idempotency-Key` e armazenado no servidor.

**Exemplos de payload:**

```json
POST /api/v1/transfer-suggestions
{
  "resourceId": "<uuid-agua>",
  "maxSuggestions": 10
}

// Resposta ilustrativa
{
  "suggestions": [
    {
      "originCenterId": "<uuid-a>",
      "destinationCenterId": "<uuid-b>",
      "resourceId": "<uuid-agua>",
      "quantity": 150,
      "reason": "Origem preserva reserva mínima; destino tem déficit planejável de 150"
    }
  ]
}
```

```json
POST /api/v1/missions
{
  "transferId": "<uuid-transferencia-aprovada>",
  "volunteerId": "<uuid-voluntario-disponivel>"
}
```

## 7. Modelo de dados mínimo

| Entidade | Campos essenciais | Dono |
|---|---|---|
| `centers` | id, name, region, created_at | Dev 1 |
| `resources` | id, name, unit, estimated_unit_weight_kg | Dev 1 |
| `stock_balance` | center_id, resource_id, on_hand, reserved, version | Dev 1 |
| `stock_movements` | id, center_id, resource_id, quantity_delta, type, reason, operation_id, created_at | Dev 1 |
| `needs` | id, center_id, resource_id, target, minimum_reserve, priority, updated_at | Dev 1 |
| `transfers` | id, origin_center_id, destination_center_id, resource_id, quantity, status, reason, version, created_at | Dev 1 |
| `transfer_events` | id, transfer_id, event_type, operation_id, metadata, created_at | Dev 1 |
| `volunteers` | id, demo_alias, vehicle_type, capacity_kg, available | Dev 3 |
| `missions` | id, transfer_id, volunteer_id, status, created_at, updated_at | Dev 3 |
| `processed_operations` | operation_id UNIQUE, action_type, entity_id, outcome, created_at | Dev 1 + Dev 3, único padrão compartilhado |

**Sugestão técnica:** migrations SQL versionadas, índices em centro/recurso/status, limites e constraints no banco, transações no Spring para aprovação/retirada/recebimento. Manter o banco simples, sem microserviços.

## 8. Backlog priorizado e dependências

### Bloco A — 09 a 11/11: contratos e fundações

| Issue | Dono | Tarefa | Depende de | Aceite |
|---|---|---|---|---|
| 404-01 | Equipe | Aprovar escopo P0, responsabilidades e critérios | Reunião | Ata validada |
| 404-02 | Dev 1 | Criar estrutura do backend e migrations núcleo | 404-01 | App inicia e migra banco |
| 404-03 | Dev 2 | Criar estrutura do frontend e layout base | 404-01 | Build e navegação base |
| 404-04 | Dev 3 | Organizar monorepo, compose, CI e docs | 404-01 | Builds em CI e execução local |
| 404-05 | Dev 1 + Dev 3 | Fechar contrato API e `TransferExecutionService` | 404-01 | `api-contract.md` revisado |

### Bloco B — 12 a 16/11: demonstração integrada das propostas

| Issue | Dono | Tarefa | Depende de | Aceite |
|---|---|---|---|---|
| 404-06 | Dev 1 | Centros, recursos, estoque e necessidades REST | 404-02/05 | API CRUD + testes |
| 404-07 | Dev 1 | Excedente/déficit + sugestões | 404-06 | Teste A→B 150, A→C 100 |
| 404-08 | Dev 2 | Telas de centros, estoque e necessidades | 404-03/05 | Consulta API com estados de erro |
| 404-09 | Dev 2 | Dashboard e sugestões integradas | 404-07/08 | Fluxo navegável |
| 404-10 | Dev 3 | Seed sintético e smoke test integrado | 404-04/07 | Teste manual reproduzível |
| 404-11 | Equipe | Gate de 16/11 | 404-09/10 | Demo integrada de cálculo funcionando |

### Bloco C — 17 a 22/11: concluir os três pilares

| Issue | Dono | Tarefa | Depende de | Aceite |
|---|---|---|---|---|
| 404-12 | Dev 1 | Aprovação/rejeição com reserva atômica | 404-07 | Concorrência e invariantes passam |
| 404-13 | Dev 1 | Pickup/receive transacionais + eventos | 404-12 | Saldos atualizam em eventos corretos |
| 404-14 | Dev 3 | Voluntários e capacidade | 404-05 | Candidatos disponíveis/compatíveis |
| 404-15 | Dev 3 | Missões, aceite/recusa, retirada e recebimento | 404-13/14 | Fluxo completo integrado |
| 404-16 | Dev 2 | Aprovação, voluntários, missões e histórico | 404-12/15 | Fluxo ponta a ponta na UI |
| 404-17 | Dev 3 + Dev 2 | Teste E2E e deploy de homologação | 404-16 | Cenário A/B/C reproduzível online |
| 404-18 | Equipe | Gate de 22/11 | 404-17 | Recebimento confirmado ou congelar novas features |

### Bloco D — 23 a 30/11: estabilização e submissão

| Issue | Dono | Tarefa | Depende de | Aceite |
|---|---|---|---|---|
| 404-19 | Dev 1 | Testes de idempotência/concorrência e bugs | 404-13 | Invariantes verificados |
| 404-20 | Dev 2 | Polimento visual, responsividade, acessibilidade | 404-16 | Fluxo sem bloqueios mobile/desktop |
| 404-21 | Dev 3 | Observabilidade básica, CI, README, deploy estável | 404-17 | Demo acessível e reproduzível |
| 404-22 | Dev 3 + Dev 2 | P1 offline **somente se liberado no gate** | 404-18 | Fila/estado pendente, sem duplicar recebimento |
| 404-23 | Equipe | Congelamento interno e ensaio da demo (27/11) | 404-19/20/21 | Sem novas features e sem falhas críticas |
| 404-24 | Equipe | Gravar vídeo de até 3 minutos e enviar | 404-23 | Links públicos testados antes de 30/11 23h59 |

## 9. Cenário de aceitação ponta a ponta

**Dados sintéticos iniciais (garrafas de água):**

| Centro | `onHand` | `minimumReserve`/`target` | Observação |
|---|---:|---:|---|
| A | 500 | 200 de reserva | Até 300 ofertáveis |
| B | 20 | 170 de meta | Déficit 150 |
| C | 50 | 150 de meta | Déficit 100 |

**Teste obrigatório:**
1. Abrir a demo com seed; exibir 500/20/50 e necessidades.
2. Gerar propostas A→B 150 e A→C 100, cada uma com justificativa.
3. Aprovar ambas; origem A mantém 500 fisicamente, 250 reservadas.
4. Atribuir a transferência B a voluntário sintético disponível com capacidade compatível; aceitar.
5. Registrar retirada de B: A passa a 350 físicas e 100 reservadas; B continua com 20 físicas.
6. Confirmar recebimento de B: B passa a 170.
7. Repetir C: depois da retirada A passa a 250 físicas e reserva 0; após recebimento C passa a 150.
8. Tentar repetir o mesmo `operationId` de recebimento; nenhum saldo muda.
9. Tentar aprovar nova transferência acima do ofertável; servidor retorna erro de domínio (`409`).
10. Ver histórico com cada estado/evento; manter a distinção entre saldo físico, reservado e em trânsito.

**Teste de falha obrigatória:** dois pedidos de aprovação simultâneos disputam o mesmo excedente. Somente quantidades válidas são reservadas; nunca há alocação superior ao ofertável.

## 10. Critérios de pronto — Definition of Done

Uma issue só pode ser concluída se:

- Critérios de aceite reproduzidos e revisados.
- Build, lint e testes relevantes passando no CI.
- Sem credenciais/segredos no GitHub; `env.example` atualizado quando necessário.
- Contrato REST e tipos TS atualizados quando a API muda.
- Mudanças de banco com migration e rollback/estratégia de correção discutidos.
- Tratamento de erro e loading na UI.
- Pull request revisado por outro integrante; sem merge direto de mudanças de domínio na branch principal.
- Demo e README continuam executáveis após a integração.

**Fluxo de branches sugerido:** `main` protegida; `feat/404-xx-descricao`; PR pequeno por issue; integração diária; dono de integração eleito. Reunião rápida diária (10–15 min) para bloqueios e interfaces.

## 11. Regras da competição a considerar

- Repositório(s) **público(s) criado(s) depois de 04/11/2026 às 21h**, sem forks ou históricos anteriores; todo código desenvolvido no evento deve estar neles.
- Código privado pré-evento **não pode ser copiado**; reutilização de código público pré-existente segue condições do item 6.2 do regulamento.
- Escolha de linguagem, backend e frontend é livre; IA não é obrigatória.
- Enviar pelo painel até **30/11/2026 às 23h59**: descrição, trilha, repositório(s), demo e vídeo de até 3 minutos sem login.
- Manter demo online durante a avaliação e GitHub público até pelo menos 15/12.
- Critérios do júri: execução técnica, implementação técnica, impacto e comunicação (10 pontos cada).
- Se finalistas, repositórios congelados entre a submissão e o início da final em 12/12.

**Fonte interna para validação:** regulamento completo fornecido pela equipe, itens 5, 6, 7, 8 e 9. A documentação final deve conter o link oficial `https://hackbrasil.com/regulamento`.

## 12. Decisões a votar na reunião (não presumir aprovação)

- [ ] Confirmar os três pilares e todas as funcionalidades RF01–RF12 como P0.
- [ ] Definir quem são Dev 1, Dev 2 e Dev 3 e quem integra/revisa a release.
- [ ] Confirmar capacidade semanal de cada integrante e horários de integração.
- [ ] Aprovar transferências de **um recurso/um voluntário** no MVP, sem divisão automática.
- [ ] Aprovar prioridade manual, peso estimado e verificação humana da viabilidade da viagem.
- [ ] Definir política básica de acesso para a demo pública e limite de mutações.
- [ ] Aprovar contrato REST e nomenclatura dos estados até 11/11.
- [ ] Decidir se P1 offline será tentado somente após o gate de 22/11.
- [ ] Definir owner da gravação do vídeo, README, deploy e submissão final.

**Regra de corte sugerida:** se o fluxo integrado de redistribuição não estiver pronto em 16/11, suspender P1. Se retirada/recebimento não estiverem prontos em 22/11, não iniciar offline/radio/mapas; concluir o P0. Se o escopo exceder a capacidade da equipe, simplificar o cadastro antes de comprometer integridade de estoque e entrega.

---

**Status deste material:** especificação inicial para debate da Equipe 404, pronta para virar issues após aprovação. **Não é uma ata de decisões já tomadas pelos três integrantes.**
