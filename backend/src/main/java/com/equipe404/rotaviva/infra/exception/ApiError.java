package com.equipe404.rotaviva.infra.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Erro padronizado das funcionalidades em /api/v1")
public record ApiError(
        @Schema(description = "Mensagem legível", example = "Já existe um coordenador com este e-mail")
        String message
) {

    public static ApiError of(String message) {
        return new ApiError(message);
    }
}
