package com.equipe404.rotaviva.core.centers.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CenterRequestTest {

    @Test
    void stripsNameAndRegion() {
        CenterRequest request = new CenterRequest("  Centro Norte ", " Zona Norte  ");

        assertEquals("Centro Norte", request.name());
        assertEquals("Zona Norte", request.region());
    }

    @Test
    void keepsNullsForBeanValidation() {
        CenterRequest request = new CenterRequest(null, null);

        assertNull(request.name());
        assertNull(request.region());
    }
}
