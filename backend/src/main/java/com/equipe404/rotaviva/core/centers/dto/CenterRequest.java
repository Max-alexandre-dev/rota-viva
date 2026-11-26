package com.equipe404.rotaviva.core.centers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de criação e edição de centro")
public record CenterRequest(
        @Schema(description = "Nome do centro; salvo sem espaços nas pontas", example = "Centro de Apoio Norte")
        @NotBlank @Size(max = 120) String name,
        @Schema(description = "Região atendida; salva sem espaços nas pontas", example = "Zona Norte")
        @NotBlank @Size(max = 120) String region,
        @Schema(description = "ID do coordenador responsável pelo centro", example = "1")
        @NotNull Long coordinatorId
) {
    public CenterRequest {
        name = name == null ? null : name.strip();
        region = region == null ? null : region.strip();
    }
}
