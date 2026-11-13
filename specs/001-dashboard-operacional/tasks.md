# Tarefas da feature 001 — propostas

> Em cada tarefa, incluir evidência (print, teste ou PR) e assinalar somente após verificação real.

## Gate de especificação (equipe)

- [ ] T00 Revisar/autorizar `spec.md`, pendências, conteúdo exibido e cenários de erro (todos).
- [ ] T01 Validar o contrato de dados mock/API e campos obrigatórios (Dev 1 + Dev 2).

## Implementação (principalmente Dev 2)

- [ ] T02 Auditar estrutura Vue existente; identificar imports e arquivos ausentes; sem refatorar regras de domínio.
- [ ] T03 Criar tokens de cores, tipografia, espaçamentos, estados de foco e componentes de base.
- [ ] T04 Implementar shell responsivo (sidebar desktop, navegação mobile, cabeçalho e cenário de demonstração).
- [ ] T05 Implementar cartões de indicadores usando dados derivados, não valores hardcoded.
- [ ] T06 Implementar lista ordenada de necessidades com vazio/loading/erro.
- [ ] T07 Implementar cartões de sugestões com explicação e ação de navegação.
- [ ] T08 Conectar o dashboard a uma store Pinia e mock padronizado; preservar integridade dos dados.
- [ ] T09 Implementar detalhe/navegação para `/transferencias`, sem botão decorativo.

## Regras, testes e integração (Dev 1 e Dev 3)

- [ ] T10 Verificar cálculo de disponibilidades, reserva mínima, prioridades e bloqueio de centros inativos (Dev 1).
- [ ] T11 Testar cenário A→B 150 e A→C 100 sem dupla alocação (Dev 1).
- [ ] T12 Criar cenários vazios, erros e conflitos, com dados sintéticos consistentes (Dev 3).
- [ ] T13 Validar experiência em 390 px, teclado, estados e build (`npm run build`) (Dev 2 + Dev 3).
- [ ] T14 Registrar evidências dos critérios AC01–AC16 em PR e fazer revisão por outro membro (todos).

## Saída do primeiro incremento

A tela do coordenador comunica corretamente déficits e sugestões, o caminho para transferências funciona e a apresentação pode demonstrar o valor sem alegar funcionalidades inexistentes.
