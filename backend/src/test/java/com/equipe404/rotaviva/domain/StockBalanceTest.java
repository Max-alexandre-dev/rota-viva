package com.equipe404.rotaviva.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockBalanceTest {
    @Test
    void calculatesOfferableStockAfterMinimumReserve() {
        StockBalance balance = new StockBalance(500, 0, 200);

        assertEquals(300, balance.calcularOfertavel());
    }

    @Test
    void reservationDoesNotChangePhysicalStock() {
        StockBalance balance = new StockBalance(500, 0, 200);

        balance.reservar(150);

        assertEquals(500, balance.getEstoqueFisico());
        assertEquals(150, balance.getReservado());
        assertEquals(150, balance.calcularOfertavel());
    }

    @Test
    void rejectsReservationAboveOfferableStock() {
        StockBalance balance = new StockBalance(500, 0, 200);

        assertThrows(IllegalArgumentException.class, () -> balance.reservar(350));
        assertEquals(0, balance.getReservado());
    }

    @Test
    void rejectsZeroReservation() {
        StockBalance balance = new StockBalance(500, 0, 200);

        assertThrows(IllegalArgumentException.class, () -> balance.reservar(0));
    }

    @Test
    void rejectsNegativeReservation() {
        StockBalance balance = new StockBalance(500, 0, 200);

        assertThrows(IllegalArgumentException.class, () -> balance.reservar(-1));
    }

    @Test
    void rejectsNegativeInitialValues() {
        assertThrows(IllegalArgumentException.class, () -> new StockBalance(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new StockBalance(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new StockBalance(0, 0, -1));
    }

    @Test
    void offerableStockDoesNotOverflowForLargeInputValues() {
        StockBalance balance = new StockBalance(0, Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertEquals(0, balance.calcularOfertavel());
    }

    @Test
    void accumulatedReservationsCannotExceedOfferableLimit() {
        StockBalance balance = new StockBalance(500, 0, 200);

        balance.reservar(150);
        balance.reservar(150);

        assertEquals(300, balance.getReservado());
        assertEquals(0, balance.calcularOfertavel());
        assertThrows(IllegalArgumentException.class, () -> balance.reservar(1));
        assertEquals(300, balance.getReservado());
    }
}
