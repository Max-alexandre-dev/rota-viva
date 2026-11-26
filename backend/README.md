# Backend — Rota Viva

**Stack:** Java 21 + Spring Boot 3.5.7.

O backend inclui health check e uma API demonstrativa de transferências. A API usa armazenamento em memória e identifica suas respostas com `storageMode: "DEMO_IN_MEMORY"`.

```bash
mvn spring-boot:run
# http://localhost:8080/api/health
```

## API demonstrativa de transferências

| Método | Rota | Resultado |
|---|---|---|
| GET | `/api/transfers` | Lista transferências |
| GET | `/api/transfers/{id}` | Consulta uma transferência |
| POST | `/api/transfers` | Cria proposta (201) |
| POST | `/api/transfers/{id}/approve` | Aprova e reserva saldo (200) |
| POST | `/api/transfers/{id}/reject` | Rejeita proposta (200) |

Todos os POSTs exigem `Idempotency-Key` com um UUID. Repetir a chave com o mesmo comando retorna o resultado original; reutilizá-la com outro conteúdo retorna 409. Uma nova chave para uma operação já concluída também retorna 409.

Exemplo de corpo de criação:

```json
{
  "originCenterId": "10000000-0000-4000-8000-000000000001",
  "destinationCenterId": "10000000-0000-4000-8000-000000000002",
  "resourceId": "20000000-0000-4000-8000-000000000001",
  "quantity": 25,
  "reason": "Atender demanda prioritária"
}
```

O conjunto sintético reproduzível contém os centros `10000000-0000-4000-8000-000000000001`, `10000000-0000-4000-8000-000000000002` e `10000000-0000-4000-8000-000000000003`, e o recurso `20000000-0000-4000-8000-000000000001`. O terceiro centro tem saldo ofertável de 10 unidades para verificar concorrência.

O estado desaparece ao reiniciar a aplicação e não é compartilhado entre instâncias. A sincronização e idempotência em memória protegem somente esta instância; ainda faltam banco, transações persistentes, bloqueio/versão otimista distribuída e autenticação.

### Regras para o backend

- Estoques, reservas e transferências são alterados em transações do servidor.
- Operações críticas devem ser idempotentes.
- Reservar estoque não é realizar entrega.
- Não expor endpoints mutáveis publicamente antes de autenticação/controle de demo.
