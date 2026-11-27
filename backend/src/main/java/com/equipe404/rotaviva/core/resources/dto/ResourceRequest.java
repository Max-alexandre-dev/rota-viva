package com.equipe404.rotaviva.core.resources.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Dados de criação e edição de recurso")
public record ResourceRequest(
        @Schema(description = "Nome do recurso; salvo sem espaços nas pontas", example = "Água mineral 1,5 L")
        @NotBlank @Size(max = 120) String name,
        @Schema(description = "Unidade de contagem; salva sem espaços nas pontas", example = "garrafa")
        @NotBlank @Size(max = 30) String unit,
        @Schema(description = "Peso estimado de uma unidade, em kg, com até 3 casas decimais", example = "1.500")
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 7, fraction = 3)
        BigDecimal estimatedUnitWeightKg
) {
    public ResourceRequest {
        name = name == null ? null : name.strip();
        unit = unit == null ? null : unit.strip();
    }
}
