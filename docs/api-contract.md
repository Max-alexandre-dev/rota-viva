# Contrato REST — rascunho

**Status:** proposta, não implementado. Endpoints abaixo são *candidatos*; devem ser revisados por Dev 1, Dev 2 e Dev 3 antes da integração.

| Método | Rota sugerida | Objetivo |
|---|---|---|
| GET | `/api/health` | Saúde do backend **(implementado no starter)** |
| GET | `/api/centers` | Listar centros |
| POST | `/api/centers` | Cadastrar centro (coordenador) |
| GET | `/api/resources` | Listar recursos |
| GET | `/api/inventory?centerId=...` | Consultar estoque físico e reservado |
| POST | `/api/inventory/movements` | Registrar entrada/correção autorizada |
| GET | `/api/needs` | Consultar necessidades |
| POST | `/api/needs` | Registrar meta/reserva mínima |
| POST | `/api/transfers/proposals` | Calcular propostas sem movimentar estoque |
| POST | `/api/transfers/{id}/approve` | Aprovar e reservar de modo transacional |
| POST | `/api/transfers/{id}/withdraw` | Confirmar retirada e baixa na origem |
| POST | `/api/transfers/{id}/receive` | Confirmar recebimento no destino |

## Regras transversais

- Mudanças críticas de estoque precisam ocorrer em **transações atômicas** no backend.
- Operações de escrita críticas requerem identificador idempotente; reuso com conteúdo divergente deve produzir conflito para revisão.
- Não usar a interface do frontend como fonte de verdade para estoque global.
- Resposta 409 deve incluir um código de erro compreensível e ser mostrada ao operador.

**Não implementar todos os endpoints automaticamente.** Fechar estrutura de payloads e estados na reunião técnica.
