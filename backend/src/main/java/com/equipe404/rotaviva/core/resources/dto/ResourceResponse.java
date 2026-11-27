package com.equipe404.rotaviva.core.resources.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Recurso cadastrado")
public record ResourceResponse(
        @Schema(description = "Identificador gerado pela aplicação", example = "20000000-0000-4000-8000-000000000001")
        UUID id,
        @Schema(example = "Água mineral 1,5 L")
        String name,
        @Schema(example = "garrafa")
        String unit,
        @Schema(description = "Peso estimado de uma unidade, em kg", example = "1.500")
        BigDecimal estimatedUnitWeightKg
) {
}
