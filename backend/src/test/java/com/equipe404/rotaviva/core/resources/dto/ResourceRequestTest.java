package com.equipe404.rotaviva.core.resources.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResourceRequestTest {

    @Test
    void stripsNameAndUnit() {
        ResourceRequest request = new ResourceRequest("  Água ", " garrafa  ", BigDecimal.ONE);

        assertEquals("Água", request.name());
        assertEquals("garrafa", request.unit());
    }

    @Test
    void keepsNullsForBeanValidation() {
        ResourceRequest request = new ResourceRequest(null, null, null);

        assertNull(request.name());
        assertNull(request.unit());
    }
}
