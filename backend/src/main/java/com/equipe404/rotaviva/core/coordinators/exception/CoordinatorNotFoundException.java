package com.equipe404.rotaviva.core.coordinators.exception;

import com.equipe404.rotaviva.infra.exception.NotFoundException;

public class CoordinatorNotFoundException extends NotFoundException {

    public CoordinatorNotFoundException(Long id) {
        super("Coordenador " + id + " não encontrado");
    }
}
