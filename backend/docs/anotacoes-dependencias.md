# Anotações e dependências — Back-end Rota Viva

## Objetivo

Reduzir código repetitivo com anotações, recursos do Java e bibliotecas adequadas, mantendo regras de negócio explícitas e legíveis. A organização segue [clean-architecture.md](./clean-architecture.md) e os princípios de [solid.md](./solid.md).

## Dependências e situação atual

| Recurso | Uso | Situação no `pom.xml` inspecionado |
|---|---|---|
| Spring Web | Controllers REST, JSON e tratamento de erros | Declarado |
| Spring Data JPA | Repositórios e persistência | Declarado; em uso no cadastro de coordenadores |
| Bean Validation | Validação declarativa de DTOs | Declarado pelo starter de validação |
| PostgreSQL e Flyway | Banco e migrations versionadas | Declarados |
| Spring Boot Starter Test | Testes com JUnit e Mockito | Declarado |
| Lombok | Getters e construtores | Declarado, com processador configurado no `maven-compiler-plugin` |
| MapStruct | Conversão de entidades e DTOs em compilação | Declarado (1.6.3), com `lombok-mapstruct-binding` no processamento de anotações |

Priorizar os recursos já disponíveis. Adotar Lombok para código mecânico e MapStruct quando a quantidade ou complexidade das conversões justificar. Este documento não instala dependências.

## Lombok

Dependência proposta: `org.projectlombok:lombok`. Escolher uma versão compatível com Java 21 e com o gerenciamento de dependências do projeto; não copiar versões de exemplos antigos.

| Anotação | Uso recomendado |
|---|---|
| `@Getter` | Acesso de leitura aos campos de entidades |
| `@Setter` | Apenas em campos cuja alteração direta seja permitida |
| `@RequiredArgsConstructor` | Injeção por construtor para dependências `final` em serviços e controllers |
| `@NoArgsConstructor(access = AccessLevel.PROTECTED)` | Construtor sem argumentos necessário para entidades JPA |
| `@Builder` | Construção com muitos parâmetros, quando melhorar a clareza e preservar validações |

O Lombok gera métodos e construtores durante a compilação. A combinação escolhida deve corresponder ao comportamento desejado da classe. Consulte a [documentação de anotações do Lombok](https://projectlombok.org/features/).

Exemplo ilustrativo de serviço concreto em `service/`, omitindo operações de negócio:

```java
@Service
@RequiredArgsConstructor
public class CoordinatorService {
    private final CoordinatorRepository repository;
}
```

Os imports correspondentes são `org.springframework.stereotype.Service` e `lombok.RequiredArgsConstructor`.

### Cuidados com entidades

- Não aplicar `@Data` indiscriminadamente em entidades JPA: ele também gera setters, igualdade e representação textual. Prefira selecionar as anotações necessárias.
- Não gerar `equals`, `hashCode` ou `toString` envolvendo todos os relacionamentos. Defina igualdade conforme a identidade da entidade, considerando IDs ainda não atribuídos e proxies.
- Não expor setters para saldo, reserva ou status quando esses campos devem mudar por operações validadas.
- Não usar um builder para contornar construtores ou métodos que protegem invariantes.
- Não usar `@SneakyThrows` para esconder decisões sobre tratamento de erros.

O comportamento combinado de `@Data` está descrito na [documentação oficial](https://projectlombok.org/features/Data).

## DTOs com records do Java

Para DTOs simples, preferir `record`, disponível no Java 21 do projeto. Ele fornece construtor, acessores e métodos de valor sem Lombok. Não usar records como entidades JPA.

Exemplo de formato de entrada, sem definir um novo endpoint:

```java
public record CoordinatorCreateRequest(
        @NotBlank String name,
        @NotBlank @Email String email
) {}
```

As anotações são de `jakarta.validation.constraints`. Um record não torna objetos internos automaticamente imutáveis; coleções podem exigir cópias defensivas.

## Validação declarativa

Usar `spring-boot-starter-validation`, já declarado, para evitar verificações repetidas de formato nos controllers.

| Anotação | Finalidade |
|---|---|
| `@NotNull` | Exigir um valor não nulo |
| `@NotBlank` | Exigir texto não vazio e não composto somente por espaços |
| `@Email` | Validar formato de e-mail; combinar com obrigatoriedade quando necessário |
| `@Size` | Limitar tamanho de texto ou coleção conforme o contrato |
| `@Positive` | Exigir valor maior que zero |
| `@PositiveOrZero` | Permitir zero e impedir valores negativos |
| `@Valid` | Acionar validação do DTO recebido e validação em cascata |

Restrições numéricas não substituem `@NotNull` em campos que aceitam nulo. Validação de formato também não substitui regras como saldo disponível, transição permitida ou unicidade no banco.

## Anotações do Spring e JPA

Usar as anotações dentro das pastas da própria funcionalidade em `core/`:

| Pasta | Anotações e recursos |
|---|---|
| `controller` | `@RestController`, `@RequestMapping`, mapeamentos HTTP e `@Valid` |
| `service` | Classes concretas com `@Service`, `@RequiredArgsConstructor` e `@Transactional` para operações persistentes atômicas |
| `entity` | `@Entity`, `@Table`, `@Id`, `@Column` e relacionamentos necessários |
| `repository` | Interfaces Spring Data JPA para operações de persistência |
| `dto` | Records e restrições de Bean Validation |
| `mapper` | Conversão manual ou interface MapStruct |

Em `infra/exception`, usar `@RestControllerAdvice` e `@ExceptionHandler` para centralizar respostas de erro. Em `infra/configuration`, usar `@Configuration` e `@Bean` quando a configuração explícita for necessária.

Orientações de persistência:

- Usar `@Enumerated(EnumType.STRING)` quando o banco armazenar nomes dos enums; preservar compatibilidade ao renomear valores.
- Usar `@Version` quando houver controle otimista de concorrência definido para a entidade.
- Definir `@GeneratedValue` conforme o tipo de identificador e a estratégia da modelagem, não por cópia de outro módulo.
- Manter constraints e schema em migrations; anotações não substituem o versionamento do banco.
- Colocar `@Transactional` no serviço que coordena a operação completa. Considerar rollback e a interceptação do Spring; chamadas internas ao próprio objeto não ativam uma nova interceptação.

Não criar implementações manuais de CRUD quando o Spring Data já atender à necessidade. Consultas e regras específicas continuam explícitas.

### JPQL para consultas com nomes extensos

Usar métodos derivados do Spring Data para consultas simples. Quando a combinação de filtros tornar o nome do método longo ou difícil de entender, preferir JPQL com `@Query` e um nome curto que expresse a intenção da consulta.

- Manter a consulta no repositório da funcionalidade.
- Referenciar entidades e atributos Java no JPQL, não nomes de tabelas e colunas.
- Usar parâmetros nomeados com `@Param`, sem concatenar valores recebidos na consulta.
- Não trocar automaticamente por SQL nativo: usar JPQL quando ele atender à necessidade.

Exemplo ilustrativo de uma consulta que combina origem, destino e status:

```java
@Query("""
        select t from Transfer t
        where t.centroOrigemId = :originId
          and t.centroDestinoId = :destinationId
          and t.status = :status
        """)
List<Transfer> findByRouteAndStatus(
        @Param("originId") UUID originId,
        @Param("destinationId") UUID destinationId,
        @Param("status") TransferStatus status);
```

O exemplo pressupõe `Transfer` mapeada como entidade JPA com esses atributos; ajustar os caminhos ao mapeamento real. Usar `org.springframework.data.jpa.repository.Query` e `org.springframework.data.repository.query.Param`.

## MapStruct para mapeamentos repetitivos

MapStruct é uma opção para gerar conversões verificadas na compilação. Quando adotado, os mapeadores permanecem em `core/<funcionalidade>/mapper`.

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CoordinatorMapper {
    CoordinatorResponse toResponse(Coordinator coordinator);
}
```

O exemplo pressupõe tipos compatíveis existentes; os imports são `org.mapstruct.Mapper` e `org.mapstruct.ReportingPolicy`. Campos com nomes diferentes exigem mapeamento explícito. Campos ignorados devem ser intencionais. O mapper não consulta repositórios nem altera regras de negócio.

A configuração envolve `org.mapstruct:mapstruct` e o processador `org.mapstruct:mapstruct-processor`. Ao combinar com Lombok moderno, configurar também `org.projectlombok:lombok-mapstruct-binding`, conforme o [guia oficial de integração](https://mapstruct.org/documentation/stable/reference/html/#_lombok).

Conversões pequenas podem continuar manuais na pasta `mapper`. Evitar duas bibliotecas de mapeamento no mesmo projeto sem motivo concreto.

## Configuração e verificação

Lombok e MapStruct dependem do processamento de anotações. Ao adicioná-los, configurar o Maven para executar os processadores necessários e manter as versões compatíveis. Se houver uma lista explícita de processadores no compilador, incluir todos os utilizados.

A compilação deve funcionar pelo Maven, sem depender apenas do suporte da IDE. Não editar nem manter manualmente classes geradas por processadores.

Após configurar dependências, executar `mvn clean test` em `backend/` para verificar compilação e testes. Para MapStruct, conferir especialmente campos ignorados, conversões e comportamento com valores nulos.

Não adicionar bibliotecas para substituir poucas linhas claras. Anotações devem reduzir repetição, sem ocultar regras de estoque, autorização, idempotência ou transações.
