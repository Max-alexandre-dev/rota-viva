package com.equipe404.rotaviva.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a stock balance and the quantity protected by existing
 * reservations and the minimum reserve.
 */
public final class StockBalance {
    private final UUID centroId;
    private final UUID recursoId;
    private final int estoqueFisico;
    private volatile int reservado;
    private final int reservaMinima;

    /**
     * Creates an unbound balance for compatibility with quantity-only stock
     * calculations. A transfer approval requires the identified constructor.
     */
    public StockBalance(int estoqueFisico, int reservado, int reservaMinima) {
        this(null, null, estoqueFisico, reservado, reservaMinima, true);
    }

    public StockBalance(UUID centroId, UUID recursoId, int estoqueFisico,
                        int reservado, int reservaMinima) {
        this(Objects.requireNonNull(centroId, "centroId não pode ser nulo"),
                Objects.requireNonNull(recursoId, "recursoId não pode ser nulo"),
                estoqueFisico, reservado, reservaMinima, true);
    }

    private StockBalance(UUID centroId, UUID recursoId, int estoqueFisico,
                         int reservado, int reservaMinima, boolean ignorado) {
        this.centroId = centroId;
        this.recursoId = recursoId;
        this.estoqueFisico = validarNaoNegativo(estoqueFisico, "estoqueFisico");
        this.reservado = validarNaoNegativo(reservado, "reservado");
        this.reservaMinima = validarNaoNegativo(reservaMinima, "reservaMinima");
    }

    public UUID getCentroId() {
        return centroId;
    }

    public UUID getRecursoId() {
        return recursoId;
    }

    public int calcularOfertavel() {
        long ofertavel = (long) estoqueFisico - reservado - reservaMinima;
        return (int) Math.max(0L, ofertavel);
    }

    /**
     * Adds a reservation without changing the physical stock.
     *
     * @throws IllegalArgumentException if the quantity is not positive or
     *                                  exceeds the currently offerable stock
     */
    public synchronized void reservar(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser positiva");
        }
        if (quantidade > calcularOfertavel()) {
            throw new IllegalArgumentException("A quantidade excede o estoque ofertável");
        }
        reservado += quantidade;
    }

    public int getEstoqueFisico() {
        return estoqueFisico;
    }

    public int getReservado() {
        return reservado;
    }

    public int getReservaMinima() {
        return reservaMinima;
    }

    private static int validarNaoNegativo(int valor, String nome) {
        if (valor < 0) {
            throw new IllegalArgumentException(nome + " não pode ser negativo");
        }
        return valor;
    }
}
