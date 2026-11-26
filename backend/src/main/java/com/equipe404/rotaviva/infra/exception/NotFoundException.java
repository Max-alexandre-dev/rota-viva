package com.equipe404.rotaviva.infra.exception;

/** Registro inexistente. Traduzida para 404. */
public class NotFoundException extends BusinessException {

    public NotFoundException(String code, String message) {
        super(code, message);
    }
}
