package com.equipe404.rotaviva.core.coordinators.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Coordenador cadastrado")
public record CoordinatorResponse(
        @Schema(description = "Identificador gerado pelo banco", example = "1")
        Long id,
        @Schema(example = "Ana Souza")
        String name,
        @Schema(description = "E-mail normalizado", example = "ana.souza@example.org")
        String email,
        @Schema(description = "Data e hora de criação (UTC)", example = "2026-11-26T22:50:00Z")
        Instant createdAt
) {
}
