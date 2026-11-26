# Autenticação e segurança com JWT

## Padrão

Usar Spring Security para autenticação e autorização, com access tokens JWT enviados no cabeçalho `Authorization: Bearer <token>`. Usar HTTPS fora do ambiente local.

Este documento orienta a implementação; autenticação e dependências de segurança ainda não estão implementadas no back-end inspecionado. Seguir [clean-architecture.md](./clean-architecture.md) e o prefixo `/api/v1` definido em [swagger-api.md](./swagger-api.md).

## Dependências

- `spring-boot-starter-security`: configuração de segurança e autenticação.
- `spring-boot-starter-oauth2-resource-server`: suporte à validação de access tokens JWT.
- `spring-security-test`, com escopo de teste: testes de autenticação e autorização.

Usar as versões gerenciadas pelo Spring Boot do projeto. Preferir `JwtDecoder` e os filtros fornecidos pelo Spring Security a um filtro JWT escrito manualmente. Não adicionar várias bibliotecas JWT para a mesma finalidade.

O Resource Server valida tokens, inclusive JWTs próprios; ele não cria automaticamente login, emissão de tokens ou renovação. Consulte a [documentação oficial do Spring Security](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

## Organização

- `infra/configuration/SecurityConfig`: `SecurityFilterChain`, regras de acesso, CORS e política de sessão.
- `infra/security`: configuração de validação JWT, conversão de authorities e tratamento de falhas de segurança.
- `core/auth/controller`, `service` e `dto`: autenticação local e emissão de tokens, somente se essa opção for adotada.
- `core/coordinators`: cadastro e regras do coordenador; serviços concretos com `@Service`, sem interface obrigatória ou sufixo `Impl`.

Manter configurações criptográficas em `infra`; não colocar regras de estoque ou permissões de negócio nos filtros.

## Origem da identidade

Definir uma única origem de autenticação antes de implementar o login:

- **Provedor de identidade:** a API valida access tokens emitidos pelo provedor e associa a identidade externa ao coordenador. Em aplicações de navegador, usar o fluxo apropriado do provedor, como Authorization Code com PKCE; não usar ID token como access token da API.
- **Autenticação local:** validar credenciais no servidor e emitir tokens apenas após autenticação bem-sucedida. Usar os componentes de autenticação do Spring Security e um emissor JWT configurado, como `JwtEncoder`.

A modelagem atual de `coordinators` possui identificador, nome, e-mail e data de criação. Ela não define senha, perfil, conta bloqueada ou vínculo com provedor externo. Qualquer credencial ou associação necessária exige atualização explícita da modelagem e migration. Não tratar somente o e-mail informado pelo cliente como prova de identidade.

Cadastro público, recuperação de senha, refresh tokens e perfis de acesso não são incluídos automaticamente por este documento.

## Validação do access token

- Verificar assinatura com chave confiável e uma lista explícita de algoritmos permitidos; rejeitar tokens sem assinatura.
- Exigir e validar emissor (`iss`), público da API (`aud`), sujeito (`sub`) e expiração (`exp`).
- Validar `nbf`, quando presente, e definir tolerância pequena e explícita para diferenças de relógio.
- Configurar tempo de vida curto e documentado; não emitir tokens sem expiração.
- Usar `sub` estável para identificar a conta. Para IDs locais numéricos, representá-los como texto nessa claim.
- Aceitar chaves apenas da configuração ou do provedor confiável; não buscar URLs arbitrárias indicadas pelo token.
- Definir rotação de chaves e preservar a validação dos tokens vigentes durante a transição planejada.

Decodificar o payload não equivale a validar o token. JWT assinado não oculta seu conteúdo: incluir somente dados mínimos, sem senhas ou segredos. Essas diretrizes seguem as [boas práticas de JWT da RFC 8725](https://www.rfc-editor.org/rfc/rfc8725).

## Autorização

Proteger endpoints de negócio por padrão. Liberar somente rotas públicas explicitamente necessárias, sem um `permitAll` amplo para `/api/**`.

Definir permissões por ação e recurso. Quando forem usados perfis ou scopes, mapear claims verificadas para authorities de maneira explícita. Usar `@EnableMethodSecurity` e `@PreAuthorize` quando houver regras de autorização em métodos.

Autenticação válida não autoriza acesso a qualquer centro ou operação. Verificar no serviço os vínculos e limites de acesso aplicáveis. Obter o coordenador autor da identidade autenticada; não confiar em `coordinatorId` recebido no corpo para registrar autoria.

O modelo atual não define todas essas permissões. Não inventar papéis ou permitir acesso irrestrito por falta de uma regra definida.

## Credenciais locais, se adotadas

- Armazenar somente hashes de senha com `PasswordEncoder` e algoritmo adaptativo apropriado; nunca texto puro, criptografia reversível ou hash rápido simples.
- Configurar o custo do algoritmo conforme o ambiente e prever atualização dos hashes.
- Retornar mensagem genérica para credenciais inválidas, sem revelar se o e-mail existe.
- Limitar tentativas de autenticação e tratar abuso sem bloquear indiscriminadamente usuários legítimos.
- Não registrar senha, token completo, cookie de autenticação ou chave privada em logs.
- Guardar chaves e segredos fora do repositório, usando configuração segura do ambiente.

## Navegador, sessão e CSRF

Para access tokens enviados exclusivamente no cabeçalho Bearer, preferir armazenamento em memória no cliente e configurar a API sem sessão HTTP de autenticação. Não persistir tokens automaticamente em `localStorage`; avaliar o risco de exposição a scripts.

Se houver cookies de autenticação ou refresh, usar `HttpOnly`, `Secure`, `SameSite` adequado e escopo restrito. Proteger os endpoints que usam credenciais enviadas automaticamente pelo navegador contra CSRF. Não desativar CSRF globalmente apenas porque a aplicação usa JWT.

Configurar CORS com origens, métodos e cabeçalhos necessários. Não combinar credenciais com origem curinga. CORS não substitui autenticação ou autorização. As considerações de armazenamento e revogação estão no [guia JWT da OWASP](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_Cheat_Sheet.html).

## Expiração, renovação e logout

Sem refresh token, exigir nova autenticação ao expirar o access token. Apagar o token no cliente não invalida cópias já emitidas no servidor.

Caso a renovação seja necessária, definir armazenamento seguro, expiração, rotação, detecção de reutilização e revogação de refresh tokens. Na persistência local, armazenar somente seu hash. Não aceitar refresh token como credencial para endpoints de negócio.

Se logout, bloqueio de conta ou mudança de permissão precisarem ter efeito imediato, adotar um mecanismo de revogação ou consulta de estado compartilhado entre instâncias. Documentar o prazo de efetivação; JWT validado apenas por assinatura continua utilizável até expirar.

## Erros e Swagger

- `401 Unauthorized`: ausência de autenticação em rota protegida ou token inválido/expirado.
- `403 Forbidden`: identidade autenticada sem permissão para a ação.
- Manter mensagens e schema de erro coerentes com a API, sem expor detalhes criptográficos.

Configurar `AuthenticationEntryPoint` e `AccessDeniedHandler` para erros da cadeia de segurança. O tratamento por `@RestControllerAdvice` sozinho não cobre todas as falhas que ocorrem antes do controller.

No OpenAPI, definir esquema HTTP Bearer com `bearerFormat: JWT` e associá-lo às operações protegidas. Não marcar login ou outras rotas públicas como protegidas por engano. A documentação não implementa segurança. Definir a exposição do Swagger por ambiente e não inserir tokens reais nos exemplos.

## Verificação

Testar ausência de token, expiração, assinatura inválida, emissor e público incorretos, acesso permitido e acesso negado ao recurso. Testar também login e renovação se forem implementados.

Testes com principal JWT simulado verificam autorização, mas não comprovam validação criptográfica. Incluir testes com tokens assinados e o decoder configurado. Quando houver cookies, incluir cenários CSRF; verificar também a configuração CORS adotada.
