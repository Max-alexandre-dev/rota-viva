package com.equipe404.rotaviva.core.coordinators.exception;

import com.equipe404.rotaviva.infra.exception.ConflictException;

public class CoordinatorEmailAlreadyExistsException extends ConflictException {

    public CoordinatorEmailAlreadyExistsException() {
        super("COORDINATOR_EMAIL_CONFLICT", "Já existe um coordenador com este e-mail");
    }
}
