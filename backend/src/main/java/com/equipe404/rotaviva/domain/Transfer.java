package com.equipe404.rotaviva.domain;

import java.util.Objects;
import java.util.UUID;

/** A proposed movement of a resource between two centers. */
public final class Transfer {
    private final UUID id;
    private final UUID centroOrigemId;
    private final UUID centroDestinoId;
    private final UUID recursoId;
    private final int quantidade;
    private final String justificativa;
    private volatile TransferStatus status;

    private Transfer(UUID id, UUID centroOrigemId, UUID centroDestinoId,
                     UUID recursoId, int quantidade, String justificativa) {
        this.id = Objects.requireNonNull(id, "id não pode ser nulo");
        this.centroOrigemId = Objects.requireNonNull(centroOrigemId, "centro de origem não pode ser nulo");
        this.centroDestinoId = Objects.requireNonNull(centroDestinoId, "centro de destino não pode ser nulo");
        this.recursoId = Objects.requireNonNull(recursoId, "recurso não pode ser nulo");
        if (centroOrigemId.equals(centroDestinoId)) {
            throw new IllegalArgumentException("Os centros de origem e destino devem ser diferentes");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser positiva");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("A justificativa não pode ser vazia");
        }

        this.quantidade = quantidade;
        this.justificativa = justificativa;
        this.status = TransferStatus.PROPOSTA;
    }

    /** Creates a proposal with a stable, generated UUID. */
    public static Transfer propor(UUID centroOrigemId, UUID centroDestinoId,
                                  UUID recursoId, int quantidade, String justificativa) {
        return new Transfer(UUID.randomUUID(), centroOrigemId, centroDestinoId,
                recursoId, quantidade, justificativa);
    }

    /**
     * Approves this proposal and reserves its quantity in the origin balance.
     * The physical stock remains unchanged.
     */
    public synchronized void aprovar(StockBalance saldoOrigem) {
        exigirProposta();
        Objects.requireNonNull(saldoOrigem, "saldo de origem não pode ser nulo");
        if (!centroOrigemId.equals(saldoOrigem.getCentroId())
                || !recursoId.equals(saldoOrigem.getRecursoId())) {
            throw new IllegalArgumentException("O saldo deve corresponder à origem e ao recurso da transferência");
        }

        // reservar valida disponibilidade antes de alterar o saldo; só então o estado muda.
        saldoOrigem.reservar(quantidade);
        status = TransferStatus.APROVADA;
    }

    /** Rejects this proposal without modifying any stock balance. */
    public synchronized void rejeitar() {
        exigirProposta();
        status = TransferStatus.REJEITADA;
    }

    private void exigirProposta() {
        if (status != TransferStatus.PROPOSTA) {
            throw new IllegalStateException("A transferência só pode ser alterada enquanto estiver proposta");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getCentroOrigemId() {
        return centroOrigemId;
    }

    public UUID getCentroDestinoId() {
        return centroDestinoId;
    }

    public UUID getRecursoId() {
        return recursoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public TransferStatus getStatus() {
        return status;
    }
}
