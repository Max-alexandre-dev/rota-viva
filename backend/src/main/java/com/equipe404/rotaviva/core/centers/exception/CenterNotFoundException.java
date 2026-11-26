package com.equipe404.rotaviva.core.centers.exception;

import com.equipe404.rotaviva.infra.exception.NotFoundException;

import java.util.UUID;

public class CenterNotFoundException extends NotFoundException {

    public CenterNotFoundException(UUID id) {
        super("CENTER_NOT_FOUND", "Centro " + id + " não encontrado");
    }
}
