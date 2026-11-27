package com.equipe404.rotaviva.core.resources.exception;

import com.equipe404.rotaviva.infra.exception.NotFoundException;

import java.util.UUID;

public class ResourceNotFoundException extends NotFoundException {

    public ResourceNotFoundException(UUID id) {
        super("Recurso " + id + " não encontrado");
    }
}
