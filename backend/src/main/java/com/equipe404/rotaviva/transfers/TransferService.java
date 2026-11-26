package com.equipe404.rotaviva.transfers;

import com.equipe404.rotaviva.domain.StockBalance;
import com.equipe404.rotaviva.domain.Transfer;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class TransferService {
    private final DemoInventory inventory;
    private final Map<UUID, Transfer> transfers = new ConcurrentHashMap<>();
    private final Map<UUID, IdempotentOutcome> operations = new ConcurrentHashMap<>();

    public TransferService(DemoInventory inventory) {
        this.inventory = inventory;
    }

    public synchronized List<TransferResponse> list() {
        return transfers.values().stream()
                .sorted(Comparator.comparing(Transfer::getId))
                .map(TransferResponse::from)
                .toList();
    }

    public synchronized TransferResponse get(UUID id) {
        return TransferResponse.from(findTransfer(id));
    }

    public synchronized TransferResponse create(UUID operationId, TransferCreateRequest request) {
        Command command = new Command("create", request);
        return executeOnce(operationId, command, () -> {
            if (!inventory.containsCenter(request.originCenterId())) {
                throw new DemoDataNotFoundException("Centro de origem não encontrado no conjunto demonstrativo");
            }
            if (!inventory.containsCenter(request.destinationCenterId())) {
                throw new DemoDataNotFoundException("Centro de destino não encontrado no conjunto demonstrativo");
            }
            if (!inventory.containsResource(request.resourceId())) {
                throw new DemoDataNotFoundException("Recurso não encontrado no conjunto demonstrativo");
            }

            Transfer transfer = Transfer.propor(request.originCenterId(), request.destinationCenterId(),
                    request.resourceId(), request.quantity(), request.reason());
            transfers.put(transfer.getId(), transfer);
            return TransferResponse.from(transfer);
        });
    }

    public synchronized TransferResponse approve(UUID operationId, UUID transferId) {
        return executeOnce(operationId, new Command("approve", transferId), () -> {
            Transfer transfer = findTransfer(transferId);
            StockBalance balance = inventory.findBalance(transfer.getCentroOrigemId(), transfer.getRecursoId());
            if (balance == null) {
                throw new TransferConflictException("Não há saldo demonstrativo para a origem e o recurso da transferência");
            }
            try {
                transfer.aprovar(balance);
            } catch (IllegalArgumentException | IllegalStateException exception) {
                throw new TransferConflictException(exception.getMessage(), exception);
            }
            return TransferResponse.from(transfer);
        });
    }

    public synchronized TransferResponse reject(UUID operationId, UUID transferId) {
        return executeOnce(operationId, new Command("reject", transferId), () -> {
            Transfer transfer = findTransfer(transferId);
            try {
                transfer.rejeitar();
            } catch (IllegalStateException exception) {
                throw new TransferConflictException(exception.getMessage(), exception);
            }
            return TransferResponse.from(transfer);
        });
    }

    private Transfer findTransfer(UUID id) {
        Transfer transfer = transfers.get(id);
        if (transfer == null) {
            throw new TransferNotFoundException("Transferência não encontrada: " + id);
        }
        return transfer;
    }

    private TransferResponse executeOnce(UUID operationId, Command command, Supplier<TransferResponse> action) {
        if (operationId == null) {
            throw new IllegalArgumentException("O cabeçalho Idempotency-Key é obrigatório");
        }
        IdempotentOutcome previous = operations.get(operationId);
        if (previous != null) {
            if (!previous.command.equals(command)) {
                throw new TransferConflictException("Idempotency-Key já foi usado com outra operação ou conteúdo");
            }
            if (previous.response != null) {
                return previous.response;
            }
        } else {
            previous = new IdempotentOutcome(command);
            operations.put(operationId, previous);
        }

        TransferResponse response = action.get();
        previous.response = response;
        return response;
    }

    private record Command(String action, Object input) {
    }

    private static final class IdempotentOutcome {
        private final Command command;
        private TransferResponse response;

        private IdempotentOutcome(Command command) {
            this.command = command;
        }
    }
}
