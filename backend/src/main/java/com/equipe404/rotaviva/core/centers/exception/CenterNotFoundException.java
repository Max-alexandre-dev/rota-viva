package com.equipe404.rotaviva.core.centers.exception;

import com.equipe404.rotaviva.infra.exception.NotFoundException;

import java.util.UUID;

public class CenterNotFoundException extends NotFoundException {

    public CenterNotFoundException(UUID id) {
        super("Centro " + id + " não encontrado");
    }
}
