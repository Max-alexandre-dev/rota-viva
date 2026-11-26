package com.equipe404.rotaviva.domain;

/**
 * Represents a stock balance and the quantity protected by existing
 * reservations and the minimum reserve.
 */
public final class StockBalance {
    private final int estoqueFisico;
    private int reservado;
    private final int reservaMinima;

    public StockBalance(int estoqueFisico, int reservado, int reservaMinima) {
        this.estoqueFisico = validarNaoNegativo(estoqueFisico, "estoqueFisico");
        this.reservado = validarNaoNegativo(reservado, "reservado");
        this.reservaMinima = validarNaoNegativo(reservaMinima, "reservaMinima");
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
    public void reservar(int quantidade) {
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
