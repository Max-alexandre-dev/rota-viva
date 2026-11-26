package com.equipe404.rotaviva.core.coordinators.dto;

import java.time.Instant;

public record CoordinatorResponse(Long id, String name, String email, Instant createdAt) {
}
