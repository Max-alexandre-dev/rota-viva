# Etapas de implementação do back-end

## Como seguir o roteiro

Executar as etapas na ordem das dependências, entregando uma funcionalidade completa por vez. Inspecionar e reaproveitar o código existente; este roteiro não indica que todas as etapas estão ausentes ou concluídas.

Seguir [arquitetura](clean-architecture.md), [modelagem](modelagem-dados.md), [SOLID](solid.md) e [anotações e dependências](anotacoes-dependencias.md). Registrar o resultado e as pendências de cada etapa antes de avançar para funcionalidades que dependam dela.

Os CRUDs abaixo são uma sequência proposta de implementação. Confirmar regras de edição, exclusão e acesso ainda não definidas antes de implementar as operações dependentes. Não criar exclusão genérica para todas as tabelas.

## Passo a passo comum de cada CRUD

1. Definir operações necessárias, campos aceitos, permissões, validações e respostas HTTP.
2. Criar ou ajustar a entidade em `core/<funcionalidade>/entity`, conforme a modelagem, declarando colunas, unicidade e índices nas anotações; o Hibernate gera o schema.
3. Criar o repositório em `repository`; usar métodos derivados simples ou JPQL quando o nome ficar extenso.
4. Criar DTOs de entrada e saída em `dto`, preferindo records e Bean Validation.
5. Criar as conversões em `mapper`, sem acesso ao banco ou regras de negócio.
6. Implementar o serviço concreto em `service`, com injeção por construtor, validações de negócio e transações necessárias.
7. Definir exceções específicas em `exception` e integrá-las ao tratamento global.
8. Implementar o controller em `controller`, usando o contrato versionado e delegando ao serviço.
9. Documentar operações e respostas no Swagger.
10. Testar sucesso, entrada inválida, registro inexistente, conflito e acesso indevido, conforme a operação.

Criar somente as pastas utilizadas. Enums ficam em `enums`; não criar interface de serviço nem classe `Impl` por padrão.

## Etapa 1 — Fundação e persistência

1. Inspecionar dependências, configurações, testes e endpoints existentes.
2. Organizar gradualmente os pacotes em `core/` e `infra/`, preservando comportamento e testes.
3. Configurar PostgreSQL por variáveis de ambiente e habilitar a persistência hoje desativada.
4. Gerar o schema pelo Hibernate (`spring.jpa.hibernate.ddl-auto: update`) a partir das entidades; adicionar cada entidade quando necessária à etapa correspondente. O projeto não usa migrations.
5. Definir constraints, índices e estratégia de concorrência para as operações previstas.
6. Configurar erros HTTP padronizados e as dependências necessárias, sem adicionar bibliotecas apenas por antecipação.

**Resultado esperado:** aplicação inicia com banco, o schema é gerado a partir das entidades e as falhas de configuração são identificáveis sem expor segredos.

## Etapa 2 — Swagger e contrato versionado

1. Seguir [swagger-api.md](swagger-api.md) para instalar e configurar springdoc.
2. Definir metadados do contrato na versão `1.0.0`.
3. Adotar `/api/v1` nos endpoints de negócio, coordenando a migração das rotas existentes com seus consumidores.
4. Verificar Swagger UI e JSON OpenAPI com os endpoints realmente disponíveis.

**Resultado esperado:** documentação acessível no ambiente de desenvolvimento, com versão, schemas e respostas corretos. Atualizá-la em todas as etapas seguintes.

## Etapa 3 — Cadastro de coordenadores

1. Definir quem pode cadastrar, consultar e editar coordenadores; cadastro público não é pressuposto.
2. Aplicar o passo a passo comum em `core/coordinators`.
3. Mapear `id` como `Long` com geração identity, nome, e-mail único e data de criação.
4. Implementar criação, listagem, consulta por ID e edição conforme o contrato definido.
5. Definir normalização do e-mail e garantir unicidade também no banco.
6. Não excluir coordenadores vinculados a operações históricas. Definir a política de remoção antes de expor essa operação; inativação exige campo e regra ainda não presentes na modelagem.

**Resultado esperado:** dados válidos e persistidos, duplicidade tratada como conflito e histórico preservado.

**Situação:** implementada em `core/coordinators`, com endpoints em `/api/v1/coordinators` (criação, listagem, consulta e edição por `PUT`). O e-mail é normalizado com `strip` e minúsculas. A tabela é gerada pelo Hibernate. Pendências: permissões de acesso (etapa 4; endpoints ainda abertos), documentação Swagger (etapa 2) e política de remoção (nenhuma exclusão exposta).

## Etapa 4 — Autenticação e autorização JWT

1. Definir autenticação local ou provedor externo conforme [autenticacao-jwt.md](autenticacao-jwt.md).
2. Ajustar a modelagem e as entidades para credenciais ou vínculo externo, somente conforme a opção escolhida.
3. Definir provisionamento inicial do coordenador sem abrir cadastro irrestrito.
4. Configurar Spring Security, validação de assinatura, emissor, público e expiração do JWT.
5. Implementar login e emissão somente se a autenticação for local; não criar emissão própria para substituir o provedor escolhido.
6. Proteger endpoints e definir as permissões de cada ação, obtendo a autoria da identidade autenticada.
7. Testar `401`, `403`, token inválido e acesso autorizado; documentar Bearer no Swagger.

**Resultado esperado:** endpoints protegidos e identidade confiável para registrar operações. Não publicar endpoints mutáveis das etapas anteriores sem proteção. Renovação e recuperação de senha entram apenas se incluídas no escopo.

## Etapa 5 — CRUD de centros

1. Aplicar o passo a passo comum em `core/centers`.
2. Mapear UUID, nome, região e data de criação.
3. Implementar criação, listagem, consulta por ID e edição.
4. Definir quais campos podem mudar quando houver operações vinculadas.
5. Bloquear exclusão de centros referenciados por saldos, necessidades ou transferências; implementar remoção de registros sem vínculos apenas se a política permitir.

**Resultado esperado:** centros disponíveis para estoque e necessidades, sem perda de integridade referencial. Não adicionar status de inativação sem atualizar o contrato e a modelagem.

## Etapa 6 — CRUD de recursos

1. Aplicar o passo a passo comum em `core/resources`.
2. Mapear UUID, nome, unidade e peso estimado por unidade; usar `BigDecimal` para o peso.
3. Implementar criação, listagem, consulta por ID e edição.
4. Definir validações de unidade e peso e a política de alteração de unidade quando já houver estoque.
5. Bloquear exclusão de recursos vinculados a registros operacionais; não apagar histórico em cascata.

**Resultado esperado:** catálogo consistente para estoque, necessidades e transferências.

## Etapa 7 — Estoque e movimentações

**Depende de:** centros, recursos e identidade autenticada.

1. Mapear `stock_balance` com chave composta de centro/recurso e `stock_movements` em `core/inventory`.
2. Implementar a persistência de `processed_operations` para idempotência, com identificação do autor, hash do comando e resultado. Definir sua responsabilidade compartilhada sem criar CRUD público dessa tabela.
3. Definir como criar o saldo inicial de cada combinação centro/recurso, inclusive em chamadas concorrentes.
4. Implementar consulta de saldo físico, reservado e disponibilidade.
5. Implementar entrada e ajuste com quantidade, motivo e identificador idempotente.
6. Persistir saldo, movimentação e operação processada na mesma transação.
7. Definir o tratamento de contagem física abaixo da reserva antes de permitir esse ajuste; a política ainda exige alinhamento.
8. Testar rollback, repetição, reutilização divergente da chave e concorrência com PostgreSQL.

**Resultado esperado:** saldo rastreável e sem lançamentos duplicados. Não oferecer edição direta do saldo nem exclusão de movimentações como CRUD comum.

## Etapa 8 — Necessidades e reserva mínima

**Depende de:** saldo existente para centro/recurso.

1. Aplicar o passo a passo comum em `core/needs`.
2. Mapear meta, reserva mínima, prioridade e data de atualização.
3. Garantir unicidade por centro/recurso e validar quantidades não negativas.
4. Implementar registro, consulta e atualização dos parâmetros.
5. Definir o efeito de alterações da reserva mínima sobre compromissos existentes, sem invalidá-los silenciosamente.
6. Centralizar os cálculos de ofertável, déficit físico e déficit planejável.
7. Definir a política de remoção antes de expor exclusão de necessidades.

**Resultado esperado:** cálculos reproduzíveis, com déficit físico separado de quantidades já comprometidas para o destino.

## Etapa 9 — Sugestões e propostas de transferência

**Depende de:** estoque, necessidades, centros e recursos.

1. Implementar sugestões com critérios determinísticos e justificativa, respeitando prioridades e limites definidos no contrato.
2. Garantir que simular sugestões não altere estoque nem reserve quantidades.
3. Mapear transferência e enum de estados em `core/transfers`, conforme a modelagem vigente.
4. Implementar criação da proposta com origem diferente do destino, recurso válido e quantidade positiva.
5. Implementar listagem e consulta por ID.
6. Preservar idempotência nas escritas críticas e os comportamentos válidos da API demonstrativa existente.

**Resultado esperado:** propostas explicáveis e persistidas, sem alteração antecipada de saldo.

## Etapa 10 — Aprovação, rejeição e cancelamento

1. Mapear eventos de transferência e integrá-los às operações processadas.
2. Aprovar somente propostas válidas, recalculando disponibilidade e déficit planejável dentro da transação.
3. Proteger a disputa pelo saldo da origem e pelo déficit do destino contra concorrência.
4. Reservar saldo e persistir estado, evento e resultado idempotente atomicamente, sem baixar o estoque físico.
5. Rejeitar propostas sem alterar saldo e registrar o evento correspondente.
6. Cancelar transferências aprovadas conforme o estado permitido, liberando a reserva uma única vez.
7. Testar transições inválidas, repetição, concorrência e falhas parciais.

**Resultado esperado:** estados e reservas consistentes. Não expor alteração livre de status ou exclusão de transferências como CRUD comum.

## Etapa 11 — Histórico e consultas operacionais

1. Disponibilizar histórico de eventos por transferência e movimentações por centro/recurso, respeitando permissões.
2. Retornar autoria, datas e dados relevantes sem expor detalhes internos ou conteúdo sensível das operações processadas.
3. Implementar filtros, ordenação e paginação conforme o contrato de consulta.
4. Consolidar indicadores necessários ao dashboard reutilizando as regras existentes.

**Resultado esperado:** rastreabilidade e consultas coerentes com a operação, sem duplicar fórmulas nos controllers.

O roteiro termina no escopo atual da modelagem. Voluntários, missões, retirada, recebimento e operação offline exigem etapas próprias e ampliação explícita do escopo.
