# Clean Architecture — Back-end Rota Viva

## 1. Objetivo e escopo

Este documento orienta o agente de IA na criação e evolução do código do back-end. Siga a estrutura de pastas e as responsabilidades definidas aqui para separar regras de negócio, coordenação de operações, API e persistência, mantendo uma aplicação simples e testável.

A abordagem recomendada é um **monólito organizado por funcionalidade, com camadas internas**. Não criar microserviços, módulos Maven separados, classes base genéricas ou interfaces para todas as classes sem necessidade concreta.

Este guia orienta a implementação; não significa que a estrutura descrita já esteja implementada. Também não aprova requisitos de produto pendentes em `../../docs/decisoes-pendentes.md`.

O escopo de dados deve acompanhar [modelagem-dados.md](./modelagem-dados.md). Não adicionar voluntários, missões, autenticação completa ou estados de execução de entrega apenas por causa da organização arquitetural.

## 2. Padrão de organização por funcionalidade

Cada funcionalidade fica dentro de `core/` e deve reunir diretamente suas pastas `entity`, `repository`, `service`, `controller`, `dto`, `enums`, `mapper` e `exception`.

Por exemplo, um módulo de usuários seguiria `core/user/entity`, `core/user/repository`, `core/user/service` e assim por diante. Esse exemplo explica a organização; não inclui um cadastro de usuários no escopo do Rota Viva.

Tecnicamente, este padrão é uma **arquitetura em camadas organizada por funcionalidade** (package by feature). Ele aplica separação de responsabilidades e princípios de código limpo, sem exigir a separação estrita de frameworks da Clean Architecture.

## 3. Organização de pacotes

Exemplo de organização dentro de `com.equipe404.rotaviva`. As demais funcionalidades seguem o mesmo padrão:

```text
rotaviva/
├── core/
│   └── coordinators/
│       ├── entity/
│       ├── repository/
│       ├── service/
│       │   └── CoordinatorService.java
│       ├── controller/
│       ├── dto/
│       ├── enums/
│       ├── mapper/
│       └── exception/
└── infra/
    ├── configuration/
    └── exception/
```

A árvore é uma referência de organização, não uma lista de arquivos já existentes ou uma autorização para implementar novos endpoints. Criar somente as pastas e classes necessárias à funcionalidade em desenvolvimento.

`core/` reúne as funcionalidades e suas regras. Neste padrão, controllers e repositórios também permanecem dentro de cada funcionalidade.

`infra/` reúne o suporte técnico compartilhado: `configuration/` para configurações do Spring e `exception/` para o tratamento global de erros HTTP. Integrações técnicas podem receber suas próprias pastas em `infra/` quando necessárias. Regras de negócio e serviços específicos continuam em `core/`.

O schema do banco é gerado pelo Hibernate a partir das entidades (`spring.jpa.hibernate.ddl-auto: update`). O projeto não usa migrations: tabelas, colunas e constraints são declaradas nas próprias entidades.

Regras obrigatórias:

- Manter `entity`, `repository`, `service`, `controller`, `dto`, `enums`, `mapper` e `exception` diretamente dentro de cada funcionalidade, sem camadas intermediárias `application`, `domain`, `api` ou `infrastructure`.
- Não criar pastas globais de entidades, repositórios, serviços ou controllers para reunir todas as funcionalidades.
- `core/inventory/entity` abriga saldos e movimentações; `core/transfers/entity` abriga transferências e seus eventos. Uma tabela auxiliar não exige um módulo independente.
- Serviços específicos permanecem na própria funcionalidade. Usar `infra` para suporte técnico compartilhado, sem mover para ela regras de negócio.
- Funcionalidades colaboram por serviços com responsabilidades claras, sem acessar controllers ou repositórios internos de outra funcionalidade.
- Evitar dependências circulares. Quando necessário, colocar a coordenação no serviço responsável pela operação completa.
- Enums ficam em `core/funcionalidade/enums`, mapeadores em `core/funcionalidade/mapper` e exceções específicas em `core/funcionalidade/exception`. Usar `enums` porque `enum` é uma palavra reservada do Java e não pode nomear um pacote.
- Usar classes concretas com `@Service` na pasta `service/`, sem sufixo `Impl`. Criar interfaces de serviço apenas quando houver necessidade concreta de implementações intercambiáveis ou de um contrato entre módulos. Não criar classes base sem necessidade concreta.

## 4. Responsabilidades e fluxo

```text
Requisição HTTP → controller → service → repository → banco
                      ↕          ↕           ↕
                     dto       entity      entity
```

| Pasta | Responsabilidade | Evitar |
|---|---|---|
| `entity` | Representar dados e relacionamentos; proteger invariantes e transições próprias da entidade | HTTP, acesso a repositórios e coordenação de operações entre módulos |
| `repository` | Consultar e persistir entidades; declarar consultas e mecanismos de bloqueio | Decidir aprovação, prioridade ou outras regras de negócio |
| `service` | Executar regras, coordenar entidades e repositórios, controlar transações e idempotência | Retornar `ResponseEntity`, manipular requisição HTTP ou acumular responsabilidades não relacionadas |
| `controller` | Receber HTTP, validar entrada, chamar serviço e construir resposta HTTP | Acessar repositório diretamente ou calcular saldos |
| `dto` | Definir dados de entrada e saída da API, com validações de formato | Persistência e regras de negócio |
| `exception` | Definir erros específicos da funcionalidade | Expor detalhes internos ao cliente |
| `enums` | Definir conjuntos fechados de valores, como status e prioridades | Misturar enums com entidades ou criar valores fora do escopo aprovado |
| `mapper` | Centralizar conversões entre entidades e DTOs da funcionalidade | Acessar repositórios, executar regras de negócio ou modificar estado durante a conversão |

### Entidades e persistência

As entidades podem usar anotações JPA quando a persistência for implementada. Não exigir versões duplicadas da mesma entidade apenas para separar JPA do domínio neste padrão.

- Entidades não são retornadas diretamente pela API: usar DTOs.
- Repositórios podem estender interfaces Spring Data JPA diretamente.
- Respeitar os tipos e relacionamentos de [modelagem-dados.md](./modelagem-dados.md).
- Declarar o schema nas entidades: `nullable`, `length`, `unique`, `@UniqueConstraint` e `@Index`. Regras que o Hibernate não gera, como constraints de check, devem ser garantidas na aplicação.
- Não criar CRUD ou repositório para toda tabela automaticamente.

### Serviços e regras de negócio

`CoordinatorService` é uma classe concreta anotada com `@Service`. Controllers e outros consumidores recebem essa classe por injeção de dependência via construtor.

Colocar `@Transactional`, quando necessário, nos métodos públicos do serviço responsável pela operação.

Usar injeção de dependências por construtor. O serviço pode receber e retornar DTOs da própria funcionalidade, mas não deve depender de tipos HTTP como `ResponseEntity` ou `HttpServletRequest`.

Manter comportamentos próprios da entidade nela e coordenar operações no serviço. Por exemplo, `StockBalance` protege a reserva de saldo, enquanto `TransferService` coordena a aprovação da transferência com o serviço de estoque.

As regras documentadas de disponibilidade são:

```text
ofertável = max(0, físico - reservado - reserva mínima)
déficit físico = max(0, meta - físico)
déficit planejável = max(0, meta - físico - entradas comprometidas)
```

Essas regras pertencem à funcionalidade responsável pelo estoque e pelas necessidades. Não copiá-las em controllers ou em outros serviços. A forma de obter entradas comprometidas deve respeitar os estados aprovados para o escopo.

Dividir serviços quando surgirem responsabilidades distintas, sem criar uma classe por método trivial. Armazenamento demonstrativo e dados sintéticos devem ficar separados das regras de negócio, dentro da funcionalidade responsável.

### Controllers e validação

- Aplicar Bean Validation nos DTOs de entrada para campos obrigatórios, formato e limites básicos.
- Revalidar invariantes nas entidades e serviços, mesmo quando a chamada não vier de HTTP.
- Converter entidades em DTOs de resposta dentro da operação do serviço, evitando depender de carregamento JPA tardio no controller.
- Manter as conversões em classes da pasta `mapper`, como `TransferMapper`. Preferir mapeamento manual simples, sem adicionar biblioteca por obrigação.
- Preservar rotas e payloads existentes durante a reorganização. Resolver divergências entre contratos propostos antes de mudar a API.

## 5. Transações, concorrência e idempotência

A unidade transacional é a operação de negócio completa, não cada chamada de repositório isolada.

Definir a fronteira transacional no método público do `service` responsável pela operação completa, usando `@Transactional` quando a persistência estiver configurada. Serviços participantes devem compartilhar a mesma transação; evitar transações independentes que confirmem apenas parte da operação. Considerar as regras de rollback e a interceptação do Spring: chamadas internas ao próprio objeto não ativam uma nova interceptação transacional.

Na aprovação de uma transferência, a operação deve:

1. Verificar o identificador idempotente e a identidade do comando.
2. Carregar transferência, saldo e dados necessários para validar o déficit do destino.
3. Proteger os dados envolvidos contra alterações concorrentes.
4. Validar estado, quantidade, disponibilidade e déficit planejável conforme as regras do produto.
5. Reservar o saldo e alterar o estado da transferência.
6. Persistir saldo, transferência, evento e resultado idempotente na mesma transação.
7. Confirmar a transação antes de responder sucesso.

Uma falha deve desfazer todas as alterações dessa operação. A aprovação altera a reserva, sem baixar o estoque físico.

Na implementação com banco:

- Usar bloqueio de linha ou controle otimista de versão com tratamento explícito de conflito.
- Proteger também a disputa pelo déficit do destino; bloquear apenas a origem não impede duas origens de atenderem o mesmo déficit em excesso.
- Definir uma ordem estável de bloqueios quando houver múltiplos registros.
- Garantir unicidade do identificador de operação no banco.
- Repetir a mesma chave e comando deve retornar o resultado confirmado; conteúdo diferente deve produzir conflito.
- O hash do comando deve considerar ação, alvo e dados relevantes em representação estável. Não depender da ordem das propriedades de um JSON bruto.
- Tratar chamadas simultâneas com a mesma chave sem duplicar reservas ou eventos.

O adaptador em memória serve à demonstração e a testes. Ele não substitui essas garantias em PostgreSQL. A política de retenção de resultados e de repetição de operações que falharam deve ser definida explicitamente antes da implementação persistente.

## 6. Erros, segurança e logs

Centralizar a tradução de exceções para HTTP em `infra/exception/GlobalExceptionHandler`, com `@RestControllerAdvice`. Exceções específicas ficam em `core/funcionalidade/exception` e não devem conhecer códigos HTTP.

| Situação | Resposta HTTP |
|---|---|
| Entrada inválida ou malformada | `400 Bad Request` |
| Credencial ausente ou inválida, quando houver autenticação | `401 Unauthorized` |
| Ação não permitida ao usuário autenticado | `403 Forbidden` |
| Recurso inexistente | `404 Not Found` |
| Conflito de estado, estoque, versão ou chave idempotente | `409 Conflict` |
| Falha inesperada | `500 Internal Server Error` |

Manter erros com código estável e mensagem útil. A evolução para o formato proposto com `code`, `message`, `details` e `timestamp` deve ser alinhada com o consumidor da API, sem quebrar silenciosamente o contrato existente.

- Não retornar stack traces, SQL ou segredos ao cliente.
- Registrar falhas inesperadas com contexto e identificador de correlação/operação.
- Evitar registrar credenciais e payloads completos com dados pessoais.
- Autenticar na fronteira e verificar autorização sobre a ação e o recurso no serviço, quando o mecanismo de acesso for definido.
- Não confiar em um `coordinatorId` recebido no corpo como prova da identidade do autor.
- A presença de `coordinators` na modelagem não define, por si só, login, senha ou política de permissões.
- Manter credenciais de banco fora do código e distinguir claramente o modo demonstrativo.

## 7. Testes

Os testes devem acompanhar os pacotes das funcionalidades em `src/test/java`.

| Nível | O que verificar |
|---|---|
| Entidades e regras, sem Spring | Cálculo de disponibilidade, quantidades inválidas, reserva mínima e transições permitidas |
| Serviços, com repositórios e colaboradores substituídos por fakes/mocks | Coordenação, erros, reenvio de comandos e ausência de efeitos quando a regra rejeita a operação |
| API | Validação, cabeçalhos, códigos HTTP, payloads e tradução de erros |
| Integração com PostgreSQL, quando implementado | Schema gerado, constraints, rollback, versões/bloqueios, aprovação concorrente e idempotência persistente |

Mocks não comprovam atomicidade nem concorrência no banco. Testes de persistência devem usar um ambiente PostgreSQL isolado, sem atingir dados reais.

Executar `mvn test` a partir de `backend/` para os testes configurados. Informar quais verificações realmente foram executadas e suas limitações.

## 8. Adoção gradual no código existente

1. Preservar os testes atuais como referência de comportamento e registrar lacunas relevantes.
2. Mover as classes de `transfers/` para `core/transfers/`, nas pastas `controller`, `service`, `dto` e `exception`; colocar configurações e tratamento global de erros em `infra/`.
3. Mover `Transfer` para `core/transfers/entity`, `TransferStatus` para `core/transfers/enums` e `StockBalance` para `core/inventory/entity`; centralizar as conversões de transferências em `core/transfers/mapper`.
4. Separar o armazenamento demonstrativo das regras de `TransferService`, mantendo cada responsabilidade na funcionalidade correspondente.
5. Adicionar repositórios e persistência quando essa etapa for implementada, com o schema gerado pelo Hibernate e transações nos serviços.
6. Ajustar imports e testes, preservando rotas e comportamento durante a reorganização.

Não misturar uma reorganização de pacotes com mudança silenciosa de regra de negócio. Uma lacuna funcional identificada deve ser tratada explicitamente, com critério de aceite e teste correspondente.
