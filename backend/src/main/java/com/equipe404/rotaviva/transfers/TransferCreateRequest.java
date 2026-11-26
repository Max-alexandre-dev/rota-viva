package com.equipe404.rotaviva.transfers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record TransferCreateRequest(
        @NotNull UUID originCenterId,
        @NotNull UUID destinationCenterId,
        @NotNull UUID resourceId,
        @NotNull @Positive Integer quantity,
        @NotBlank String reason
) {
}
