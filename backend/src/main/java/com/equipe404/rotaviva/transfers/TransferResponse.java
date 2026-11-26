package com.equipe404.rotaviva.transfers;

import com.equipe404.rotaviva.domain.Transfer;
import com.equipe404.rotaviva.domain.TransferStatus;

import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID originCenterId,
        UUID destinationCenterId,
        UUID resourceId,
        int quantity,
        String reason,
        TransferStatus status,
        String storageMode
) {
    static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getCentroOrigemId(),
                transfer.getCentroDestinoId(),
                transfer.getRecursoId(),
                transfer.getQuantidade(),
                transfer.getJustificativa(),
                transfer.getStatus(),
                DemoInventory.STORAGE_MODE
        );
    }
}
