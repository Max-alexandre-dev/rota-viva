package com.equipe404.rotaviva.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferTest {
    private final UUID origemId = UUID.randomUUID();
    private final UUID destinoId = UUID.randomUUID();
    private final UUID recursoId = UUID.randomUUID();

    @Test
    void createsValidProposalWithStableIdAndInitialStatus() {
        Transfer transfer = proposta(25);
        UUID id = transfer.getId();

        assertNotNull(id);
        assertEquals(id, transfer.getId());
        assertEquals(origemId, transfer.getCentroOrigemId());
        assertEquals(destinoId, transfer.getCentroDestinoId());
        assertEquals(recursoId, transfer.getRecursoId());
        assertEquals(25, transfer.getQuantidade());
        assertEquals("Atender demanda", transfer.getJustificativa());
        assertEquals(TransferStatus.PROPOSTA, transfer.getStatus());
    }

    @Test
    void rejectsInvalidQuantity() {
        assertThrows(IllegalArgumentException.class, () -> proposta(0));
        assertThrows(IllegalArgumentException.class, () -> proposta(-1));
    }

    @Test
    void rejectsSameOriginAndDestination() {
        assertThrows(IllegalArgumentException.class,
                () -> Transfer.propor(origemId, origemId, recursoId, 1, "Reposição"));
    }

    @Test
    void approvalReservesStockAndLeavesPhysicalQuantityUnchanged() {
        Transfer transfer = proposta(100);
        StockBalance balance = saldo(origemId, recursoId, 500, 50, 100);

        transfer.aprovar(balance);

        assertEquals(TransferStatus.APROVADA, transfer.getStatus());
        assertEquals(500, balance.getEstoqueFisico());
        assertEquals(150, balance.getReservado());
    }

    @Test
    void insufficientStockLeavesTransferAndBalanceUnchanged() {
        Transfer transfer = proposta(351);
        StockBalance balance = saldo(origemId, recursoId, 500, 50, 100);

        assertThrows(IllegalArgumentException.class, () -> transfer.aprovar(balance));

        assertEquals(TransferStatus.PROPOSTA, transfer.getStatus());
        assertEquals(50, balance.getReservado());
        assertEquals(500, balance.getEstoqueFisico());
    }

    @Test
    void rejectionDoesNotReserveStock() {
        Transfer transfer = proposta(100);
        StockBalance balance = saldo(origemId, recursoId, 500, 50, 100);

        transfer.rejeitar();

        assertEquals(TransferStatus.REJEITADA, transfer.getStatus());
        assertEquals(50, balance.getReservado());
        assertEquals(500, balance.getEstoqueFisico());
    }

    @Test
    void preventsDoubleApprovalAndDoesNotReserveTwice() {
        Transfer transfer = proposta(100);
        StockBalance balance = saldo(origemId, recursoId, 500, 50, 100);
        transfer.aprovar(balance);

        assertThrows(IllegalStateException.class, () -> transfer.aprovar(balance));

        assertEquals(TransferStatus.APROVADA, transfer.getStatus());
        assertEquals(150, balance.getReservado());
    }

    @Test
    void preventsDoubleRejection() {
        Transfer transfer = proposta(100);
        transfer.rejeitar();

        assertThrows(IllegalStateException.class, transfer::rejeitar);
        assertEquals(TransferStatus.REJEITADA, transfer.getStatus());
    }

    @Test
    void rejectsBalanceWithDifferentOriginWithoutChanges() {
        Transfer transfer = proposta(100);
        StockBalance saldoIncompativel = saldo(destinoId, recursoId, 500, 50, 100);
        int estoqueFisicoAntes = saldoIncompativel.getEstoqueFisico();
        int reservadoAntes = saldoIncompativel.getReservado();
        int ofertavelAntes = saldoIncompativel.calcularOfertavel();
        TransferStatus statusAntes = transfer.getStatus();

        assertThrows(IllegalArgumentException.class, () -> transfer.aprovar(saldoIncompativel));

        assertEquals(origemId, transfer.getCentroOrigemId());
        assertEquals(destinoId, saldoIncompativel.getCentroId());
        assertEquals(estoqueFisicoAntes, saldoIncompativel.getEstoqueFisico());
        assertEquals(reservadoAntes, saldoIncompativel.getReservado());
        assertEquals(ofertavelAntes, saldoIncompativel.calcularOfertavel());
        assertEquals(statusAntes, transfer.getStatus());
        assertEquals(TransferStatus.PROPOSTA, transfer.getStatus());
    }

    @Test
    void rejectsBalanceWithDifferentResourceWithoutChanges() {
        Transfer transfer = proposta(100);
        UUID recursoIncompativelId = UUID.randomUUID();
        StockBalance saldoIncompativel = saldo(origemId, recursoIncompativelId, 500, 50, 100);
        int estoqueFisicoAntes = saldoIncompativel.getEstoqueFisico();
        int reservadoAntes = saldoIncompativel.getReservado();
        int ofertavelAntes = saldoIncompativel.calcularOfertavel();
        TransferStatus statusAntes = transfer.getStatus();

        assertThrows(IllegalArgumentException.class, () -> transfer.aprovar(saldoIncompativel));

        assertEquals(origemId, saldoIncompativel.getCentroId());
        assertEquals(recursoIncompativelId, saldoIncompativel.getRecursoId());
        assertEquals(estoqueFisicoAntes, saldoIncompativel.getEstoqueFisico());
        assertEquals(reservadoAntes, saldoIncompativel.getReservado());
        assertEquals(ofertavelAntes, saldoIncompativel.calcularOfertavel());
        assertEquals(statusAntes, transfer.getStatus());
        assertEquals(TransferStatus.PROPOSTA, transfer.getStatus());
    }

    @Test
    void cannotApproveRejectedTransferOrRejectApprovedTransfer() {
        Transfer rejected = proposta(10);
        rejected.rejeitar();
        assertThrows(IllegalStateException.class,
                () -> rejected.aprovar(saldo(origemId, recursoId, 100, 0, 0)));

        Transfer approved = proposta(10);
        approved.aprovar(saldo(origemId, recursoId, 100, 0, 0));
        assertThrows(IllegalStateException.class, approved::rejeitar);
    }

    private Transfer proposta(int quantidade) {
        return Transfer.propor(origemId, destinoId, recursoId, quantidade, "Atender demanda");
    }

    private StockBalance saldo(UUID centroId, UUID saldoRecursoId, int fisico, int reservado, int minimo) {
        return new StockBalance(centroId, saldoRecursoId, fisico, reservado, minimo);
    }
}
