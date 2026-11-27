package com.equipe404.rotaviva.infra.exception;

/** Conflito com o estado atual, como duplicidade, versao ou transicao invalida. Traduzida para 409. */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }
}
