package com.equipe404.rotaviva.core.centers.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Centro cadastrado")
public record CenterResponse(
        @Schema(description = "Identificador gerado pela aplicação", example = "3f1c2b9e-8a4d-4c1e-9f2a-6b7d8e9f0a1b")
        UUID id,
        @Schema(example = "Centro de Apoio Norte")
        String name,
        @Schema(example = "Zona Norte")
        String region,
        @Schema(description = "ID do coordenador responsável", example = "1")
        Long coordinatorId,
        @Schema(description = "Data e hora de criação (UTC)", example = "2026-11-26T22:50:00Z")
        Instant createdAt
) {
}
