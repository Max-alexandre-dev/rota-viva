package com.equipe404.rotaviva;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {
    @Test
    void healthReturnsOk() {
        assertEquals("ok", new HealthController().health().get("status"));
    }
}
