package com.equipe404.rotaviva.core.coordinators.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

@Schema(description = "Dados de criação e edição de coordenador")
public record CoordinatorRequest(
        @Schema(description = "Nome do coordenador", example = "Ana Souza")
        @NotBlank @Size(max = 120) String name,
        @Schema(description = "E-mail único; salvo sem espaços nas pontas e em minúsculas",
                example = "ana.souza@example.org")
        @NotBlank @Email @Size(max = 254) String email
) {
    public CoordinatorRequest {
        name = name == null ? null : name.strip();
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}
