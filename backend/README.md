# Backend — Rota Viva

**Stack:** Java 21 + Spring Boot. PostgreSQL será configurado quando o modelo de domínio e contrato REST forem aprovados.

Este é somente o esqueleto da API. Contém `GET /api/health` e **não implementa ainda** o controle de estoques, centros ou transferências.

```bash
mvn spring-boot:run
# http://localhost:8080/api/health
```

## Organização prevista

`centers`, `resources`, `inventory`, `needs`, `transfers`, `missions` e `shared`. Cada módulo terá suas camadas apropriadas após fechar as regras.

### Regras para o backend

- Estoques, reservas e transferências são alterados em transações do servidor.
- Operações críticas devem ser idempotentes.
- Reservar estoque não é realizar entrega.
- Não expor endpoints mutáveis publicamente antes de autenticação/controle de demo.
