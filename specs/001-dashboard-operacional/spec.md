# SPEC-001 — Dashboard Operacional do Coordenador

**Versão:** 0.3
**Status:** Revisada — aguardando aprovação da Equipe 404 
**Responsável funcional:** Equipe 404  
**Projeto:** Rota Viva — Hack Brasil 2026  
**Etapa SDD:** SPECIFY

## 1. Problema

Durante uma emergência, o coordenador precisa identificar rapidamente quais centros estão com falta de recursos, onde existem excedentes e quais transferências podem ser analisadas. Indicadores ambíguos e ações que não deixam claro seu efeito podem causar decisões incorretas.

## 2. Objetivo e resultado esperado

Permitir que o coordenador, ao abrir a visão geral:

1. identifique necessidades prioritárias;
2. compreenda uma sugestão de transferência e sua justificativa;
3. navegue até a proposta correspondente para analisá-la;
4. distinga com clareza uma **sugestão**, uma **transferência aprovada** e uma **entrega recebida**.

Esta feature é principalmente de **consulta e navegação**; não realiza alterações de estoque nem aprova transferências diretamente no dashboard.

## 3. Perfis considerados

- **Coordenador:** consulta a rede de centros e analisa sugestões de transferência.
- **Operador do centro:** atualiza dados operacionais do próprio centro, em funcionalidades específicas; não altera nome ou status do centro.
- **Voluntário:** acompanha missões em uma feature posterior; não usa o dashboard global neste corte.

> **Limite do MVP:** se os perfis forem apenas simulados, a interface deverá indicar isso. Não afirmar que existe controle de acesso real sem autenticação e autorização implementadas.

## 4. Cenário de referência — dados sintéticos

- **Centro A:** estoque físico de 500 garrafas de água; reserva mínima de 200; nenhuma reserva de transferência existente. Quantidade ofertável: **300**.
- **Centro B:** estoque físico de 20 garrafas; meta de 170. Déficit atual: **150**.
- **Sugestão:** transferir **150 garrafas do Centro A para o Centro B**.

A proposta inicial **não** altera saldos. Uma aprovação futura poderá reservar estoque na origem; a retirada e o recebimento serão registrados no fluxo operacional próprio. O estoque físico do destino só aumenta após o recebimento confirmado.

## 5. Termos de domínio usados na tela

- **Estoque físico:** quantidade registrada como presente no centro.
- **Estoque reservado:** quantidade comprometida em transferências aprovadas, ainda não retirada.
- **Reserva mínima:** quantidade que deve permanecer na origem.
- **Quantidade ofertável:** `max(0, estoque_fisico - estoque_reservado - reserva_minima)`.
- **Déficit:** quantidade abaixo da meta, calculada pela regra de domínio aprovada; no cenário sem outros ajustes: `max(0, meta - estoque_fisico)`.
- **Déficit percentual:** `deficit / meta * 100`, válido somente para `meta > 0`.
- **Necessidade aberta:** registro de necessidade de um centro ativo com déficit positivo.
- **Sugestão de transferência:** proposta ainda não aprovada; não é entrega nem reserva.

> Estas definições descrevem a leitura necessária para a SPEC-001. O backend será a referência definitiva para cálculos de estoque e para estados das transferências. Campos ou dados inconsistentes deverão ser sinalizados, não mascarados.

## 6. Componentes visíveis

- Cabeçalho com nome da página e indicação clara de **cenário demonstrativo** ou **dados da API**.
- Indicação de conexão/atualização quando aplicável, sem declarar sincronização offline inexistente.
- Navegação em desktop e dispositivos móveis.
- Indicadores: **Centros ativos**, **Necessidades abertas**, **Tipos de recursos em falta**, **Aguardando retirada** e **Em trânsito**.
- Lista **Necessidades prioritárias** com centro, recurso, déficit, unidade e prioridade.
- Lista **Sugestões de transferência** com origem, destino, recurso, quantidade, ofertável, justificativa e estado.
- Ações **Ver detalhes** e **Analisar transferências**, quando seus destinos estiverem disponíveis.
- Estados de carregamento, vazio e falha, com mensagens compreensíveis.

## 7. Histórias de usuário e critérios de aceite

### US01 — Saber onde agir

**Como** coordenador, **quero** visualizar necessidades e indicadores claros **para** identificar rapidamente onde devo atuar.

**AC01 — Necessidades identificáveis**  
Dadas necessidades abertas, quando o dashboard for exibido, então cada item deverá mostrar centro, recurso, unidade de medida, déficit e prioridade.

**AC02 — Ordenação determinística**  
Dadas necessidades abertas, quando a lista for exibida, então deverá respeitar, nesta ordem:

1. prioridade **ALTA**, depois **MÉDIA**, depois **BAIXA**;
2. dentro da mesma prioridade, **maior déficit percentual em relação à meta** primeiro, inclusive entre recursos diferentes;
3. persistindo empate percentual, **identificador do recurso** em ordem crescente;
4. para o mesmo recurso e percentual, **maior déficit absoluto** primeiro;
5. persistindo empate, **identificador do centro** e depois **identificador da necessidade**, ambos em ordem crescente.

O percentual deve ser comparado **sem arredondamento** (arredondar apenas para exibição). Necessidades com déficit igual a zero não entram na lista. Metas iguais ou menores que zero não produzem déficit percentual válido nem necessidade aberta e devem ser tratadas conforme a validação do domínio, sem divisão por zero.

**AC03 — Sem necessidades**  
Dado que não há necessidades abertas e os dados foram carregados com sucesso, quando o coordenador abrir o dashboard, então será exibida uma mensagem útil de ausência de demandas, e não uma tabela vazia ou um erro.

**AC04 — Indicadores operacionais**

- **AC04.1 — Centros ativos:** contar somente centros com `status = ATIVO`.
- **AC04.2 — Necessidades abertas:** contar registros de necessidades com déficit positivo em centros ativos; uma necessidade para cada par centro/recurso, conforme o modelo de domínio aprovado.
- **AC04.3 — Tipos de recursos em falta:** contar **identificadores distintos de recursos** presentes nas necessidades abertas, sem duplicar o mesmo recurso em centros diferentes.
- **AC04.4 — Aguardando retirada:** contar transferências **aprovadas e ainda não retiradas**; sugestões sem aprovação não entram nessa contagem.
- **AC04.5 — Em trânsito:** contar transferências **retiradas e ainda não recebidas**.
- **AC04.6 — Origem e confiabilidade:** números exibidos devem vir de dados sintéticos **explicitamente identificados** ou de dados carregados da API. Sem registros, mostrar `0` com contexto; com falha de carregamento, mostrar **indisponível**, e nunca `0` inventado.
- **AC04.7 — Clareza:** apresentar nomes inequívocos e informações legíveis em desktop e mobile. A indicação de estados não pode depender apenas de cores.

### US02 — Entender sugestões

**Como** coordenador, **quero** entender por que uma transferência foi sugerida **para** avaliar a proposta antes de uma possível aprovação.

**AC05 — Conteúdo da proposta**  
Dada uma sugestão, quando ela aparecer no dashboard, então deverá mostrar origem, destino, recurso, unidade, quantidade sugerida, quantidade ofertável na origem, justificativa e estado textual.

**AC06 — Proposta não é movimentação**  
Dada uma sugestão ainda não aprovada, quando ela for exibida, então deverá receber o rótulo **Sugerida** (ou equivalente definido no contrato) e **não** alterar estoque, reserva ou status de entrega apenas por ter sido apresentada.

**AC07 — Ver detalhes**
Dada uma sugestão exibida no dashboard, quando o coordenador selecionar "Ver detalhes", então deverá acessar /transferencias com a sugestão identificada pelo seu ID.

**AC08 — Consistência das sugestões de transferência**
Dado que existem centros ativos, recursos disponíveis
e necessidades registradas, quando o dashboard apresentar sugestões pertencentes
ao mesmo planejamento, então deverá garantir que:

1. Origem e destino estejam ativos.
2. A quantidade sugerida seja positiva.
3. Origem e destino utilizem o mesmo recurso.
4. A soma sugerida por origem/recurso não exceda
   o estoque ofertável considerado no planejamento.
5. A soma destinada a uma necessidade não exceda
   seu déficit considerado no planejamento.
6. Dados inconsistentes sejam sinalizados e não
   apresentados como sugestões válidas.

**Observação:** o motor logístico é responsável por gerar propostas consistentes. O dashboard apresenta os resultados e seus estados; a aprovação deverá revalidar a disponibilidade no backend.

### US03 — Navegar sem perder contexto

**Como** coordenador, **quero** acessar a análise de transferências **para** continuar a operação sem precisar localizar novamente a necessidade.

**AC09 — Navegação geral**  
Quando o coordenador clicar em **Analisar transferências**, então será levado a `/transferencias`.

**AC10 — Preservação do contexto**
Dado que o coordenador acessou Transferências a partir de uma sugestão do dashboard, quando a página for carregada, então a proposta correspondente deverá estar selecionada ou destacada.
Se o ID não existir, exibir mensagem clara
e permitir consultar a lista geral.

**AC11 — Status compreensível**  
Estados operacionais deverão estar escritos em texto, sem depender apenas de cores ou ícones.

**AC12 — Responsividade**  
O dashboard deverá ser utilizável em larguras de **360–390 px** e **1280 px ou mais**, sem rolagem horizontal involuntária nem ações essenciais inacessíveis.

### US04 — Entender falhas e incertezas

**Como** coordenador, **quero** receber mensagens claras sobre erros e dados possivelmente desatualizados **para** evitar decisões baseadas em informações incorretas.

**AC13 — Erro de carregamento**  
Se uma consulta remota falhar, quando o dashboard tentar carregar os dados, então deverá mostrar uma mensagem compreensível e a ação **Tentar novamente**. Em modo demonstrativo local, não simular uma falha remota inexistente.

**AC14 — Conexão e atualização**  
Se a aplicação não puder consultar a API, então deverá distinguir dados locais/demonstrativos de dados atualizados pelo servidor; não poderá afirmar que operações offline foram sincronizadas.

**AC16 — Acessibilidade**  
Ações importantes deverão funcionar com teclado, possuir foco visível e rótulos acessíveis.

> **Rastreabilidade:** o antigo **AC15** (confirmação de ações que alteram dados e tratamento de HTTP 409) foi retirado desta SPEC de consulta e deverá ser especificado na feature de aprovação/execução de transferências. O número AC16 foi preservado para evitar renumeração silenciosa.

## 8. Fora do escopo da SPEC-001

- Cadastro/edição completos de centros e recursos.
- Aprovação, cancelamento, retirada e recebimento realizados diretamente no dashboard.
- Autenticação e permissões reais, caso ainda não implementadas.
- Mapa geográfico, roteirização e indicação automática de rotas seguras.
- Rastreamento ao vivo, rádio, satélite e sincronização offline.
- Métricas de eficiência sem fonte ou estatísticas simuladas apresentadas como reais.

## 9. Verificação da experiência

- Coordenador identifica a necessidade do Centro B e alcança os detalhes de A → B sem orientação durante um teste guiado.
- Indicadores e ordenação conferem com os dados sintéticos e as regras documentadas.
- Não existem controles com aparência de ação funcional que não executam nada.
- São conferidos teclado, foco, estados vazios, mobile (390 px) e desktop (1440 px).

## 10. Dependências e decisões pendentes

- Confirmação de perfis e autenticação/autorização do MVP.
- Contrato de API de necessidades e propostas, incluindo identificadores estáveis e estados.
- Regra final do domínio para divergências entre estoque físico, reserva e correções.
- Definição da tela `/transferencias` que recebe a seleção da sugestão.
- Decisão sobre histórico no primeiro corte ou navegação para feature posterior.

**Critério para encerrar SPECIFY:** requisitos revisados, testes de aceite compreendidos e pendências explicitamente aceitas ou resolvidas pela Equipe 404. O conteúdo acima ainda não representa aprovação coletiva.
