# Swagger/OpenAPI e versão da API

## Padrão

Documentar a API com OpenAPI e disponibilizar sua visualização pelo Swagger UI. Manter a documentação sincronizada com os controllers, DTOs, validações e respostas efetivamente implementados.

Este documento define o padrão a adotar. A dependência ainda não está no `pom.xml`; as rotas existentes não foram alteradas por este guia.

## Versionamento

- Versão inicial do contrato: **1.0.0**.
- Prefixo dos endpoints de negócio: **`/api/v1`**.
- Exemplo de caminho: `/api/v1/coordinators`, quando essa funcionalidade estiver implementada.
- Informar `1.0.0` nos metadados OpenAPI, em `info.version`.
- Mudanças incompatíveis de contrato exigem uma nova versão principal, como `/api/v2`.
- Adições compatíveis incrementam a versão secundária; correções compatíveis incrementam a versão de patch, mantendo `/api/v1`.

A versão do contrato é independente da versão Maven da aplicação e da versão da especificação OpenAPI. O endereço `/v3/api-docs` não significa que a API de negócio está na versão 3.

Os endpoints atuais usam caminhos sem versão, como `/api/transfers`. Migrar os consumidores e alinhar o contrato em `../../docs/api-contract.md` ao adotar `/api/v1`; não remover rotas utilizadas sem coordenar a mudança. O health check técnico pode permanecer em `/api/health`.

## Dependência

Usar `org.springdoc:springdoc-openapi-starter-webmvc-ui`, compatível com Spring MVC. Para o Spring Boot 3 do projeto, selecionar uma versão estável da linha compatível do springdoc 2.x, conferindo a matriz antes da instalação. Não usar automaticamente a versão mais recente destinada a outro Spring Boot. Consulte o [guia oficial do springdoc para Boot 3](https://springdoc.org/v2/getting-started.html) e a [matriz de compatibilidade](https://springdoc.org/v2/faq.html).

Fixar a versão escolhida no Maven. Não adicionar Springfox ou outra biblioteca de documentação em paralelo.

## Configuração

Centralizar os metadados em `infra/configuration/OpenApiConfig.java`:

```java
package com.equipe404.rotaviva.infra.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Rota Viva API",
        version = "1.0.0",
        description = "API de gestão de recursos e transferências entre centros de apoio"
))
public class OpenApiConfig {
}
```

Após instalar e configurar a dependência, os caminhos padrão são `/swagger-ui.html` para a interface e `/v3/api-docs` para o JSON OpenAPI, relativos ao endereço e contexto da aplicação. Esses caminhos são fornecidos pelo [springdoc](https://springdoc.org/v2/getting-started.html); não criar controllers para servi-los.

## Anotações

| Local | Anotação | Finalidade |
|---|---|---|
| Controller | `@Tag` | Agrupar operações por funcionalidade |
| Método do controller | `@Operation` | Descrever objetivo e definir um `operationId` único e estável |
| Método do controller | `@ApiResponses` e `@ApiResponse` | Documentar códigos HTTP e schemas das respostas |
| Parâmetro | `@Parameter` | Descrever filtros, identificadores e cabeçalhos |
| DTO e seus campos | `@Schema` | Descrever dados, formatos e exemplos sintéticos |

Usar anotações de `io.swagger.v3.oas.annotations`. Manter os controllers em `core/<funcionalidade>/controller` e os DTOs em `core/<funcionalidade>/dto`, conforme [clean-architecture.md](./clean-architecture.md).

## Conteúdo obrigatório das operações

- Informar método HTTP, caminho, objetivo, parâmetros e corpo esperado.
- Documentar respostas de sucesso e erros que a operação realmente pode produzir, com o schema de erro adotado pelo projeto.
- Representar corretamente respostas sem corpo, como `204`, e cabeçalhos de resposta quando relevantes, como `Location` em uma criação.
- Descrever campos obrigatórios, formatos, enums, limites e paginação quando existentes.
- Nas operações idempotentes, documentar `Idempotency-Key`, seu formato UUID, repetição com o mesmo conteúdo e conflito por reutilização com conteúdo diferente.
- Identificar respostas demonstrativas e limitações de persistência quando aplicáveis.
- Documentar autenticação e autorização somente conforme o mecanismo implementado; anotações OpenAPI não protegem endpoints.

Reaproveitar os tipos Java e as restrições Bean Validation reconhecidas pela ferramenta. Complementar com anotações quando faltar contexto, evitando descrições duplicadas e contraditórias.

Não incluir endpoints planejados como se estivessem disponíveis. Exemplos não devem conter dados pessoais reais ou credenciais.

## Manutenção

Atualizar a documentação na mesma alteração que modificar o contrato. Verificar o JSON em `/v3/api-docs` e a apresentação no Swagger UI após a implementação, conferindo versão, caminhos, schemas e respostas. Executar os testes relevantes da API para confirmar que o comportamento corresponde ao documentado.
