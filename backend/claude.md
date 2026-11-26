# Orientações do back-end

## Documentação de referência

Antes de alterar o back-end, consultar os documentos aplicáveis à tarefa:

- [Etapas de implementação](docs/etapas-implementacao.md): ordem de execução e passo a passo dos CRUDs e fluxos operacionais.
- [Modelagem de dados](docs/modelagem-dados.md): entidades, campos e relacionamentos.
- [Arquitetura](docs/clean-architecture.md): estrutura de pastas e responsabilidades.
- [SOLID](docs/solid.md): princípios de organização do código.
- [Anotações e dependências](docs/anotacoes-dependencias.md): Lombok, records, validação, JPA, JPQL e mapeamentos.
- [Swagger e versionamento](docs/swagger-api.md): documentação OpenAPI e versão da API.
- [Autenticação JWT](docs/autenticacao-jwt.md): autenticação, autorização e segurança.
- [Execução do back-end](README.md): configuração e comportamento disponível.

## Padrões de implementação

- Aplicar Clean Code, SOLID, DRY, KISS e YAGNI.
- Organizar funcionalidades em `core/`, com `entity`, `repository`, `service`, `controller`, `dto`, `enums`, `mapper` e `exception`, conforme necessário.
- Manter configurações e suporte técnico compartilhado em `infra/`.
- Usar serviços concretos com `@Service`, sem interface ou sufixo `Impl` por padrão.
- Preferir records para DTOs simples e injeção de dependências por construtor.
- Usar Lombok conforme o guia de anotações, sem `@Slf4j` e sem aplicar `@Data` indiscriminadamente em entidades JPA.
- Usar JPQL com `@Query` quando nomes de métodos derivados ficarem extensos ou difíceis de entender.
- Manter regras de negócio fora dos controllers e mapeadores.
- Preservar transações, invariantes de estoque, controle de concorrência e idempotência nas operações críticas.

## Escopo e validação

Inspecionar o código e o `pom.xml` antes de implementar. Distinguir padrões propostos nos documentos de recursos já disponíveis; não presumir que dependências, persistência, Swagger ou autenticação estejam configurados.

Implementar somente o escopo solicitado. Não criar funcionalidades, dependências ou abstrações sem necessidade. Preservar contratos existentes e coordenar mudanças incompatíveis, incluindo a adoção do prefixo `/api/v1`.

Atualizar a documentação afetada quando o comportamento mudar. Executar os testes relevantes a partir de `backend/`, usando `mvn test`, e informar resultados reais e limitações. Alterações apenas documentais não exigem testes da aplicação.
