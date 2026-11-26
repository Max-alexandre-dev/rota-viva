package com.equipe404.rotaviva.core.coordinators.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CoordinatorRequestTest {

    @Test
    void normalizesNameAndEmail() {
        CoordinatorRequest request = new CoordinatorRequest("  Ana Souza ", "  Ana.Souza@Example.ORG ");

        assertEquals("Ana Souza", request.name());
        assertEquals("ana.souza@example.org", request.email());
    }

    @Test
    void keepsNullsForBeanValidation() {
        CoordinatorRequest request = new CoordinatorRequest(null, null);

        assertNull(request.name());
        assertNull(request.email());
    }
}
