package com.equipe404.rotaviva.infra.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Erro padronizado das funcionalidades em /api/v1")
public record ApiError(
        @Schema(description = "Código estável do erro", example = "INVALID_REQUEST")
        String code,
        @Schema(description = "Mensagem legível", example = "A requisição contém dados inválidos")
        String message,
        @Schema(description = "Erros por campo, quando houver", example = "{\"email\": \"must be a well-formed email address\"}")
        Map<String, String> details,
        @Schema(description = "Momento do erro (UTC)")
        Instant timestamp
) {

    public static ApiError of(String code, String message, Map<String, String> details) {
        return new ApiError(code, message, details, Instant.now());
    }
}
