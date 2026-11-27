package com.equipe404.rotaviva.core.resources.controller;

import com.equipe404.rotaviva.core.resources.repository.ResourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerIntegrationTest {

    private static final String BASE = "/api/v1/resources";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ResourceRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void createsResourceWithStrippedFields() throws Exception {
        create("  Água mineral ", " garrafa ", "1.500")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(BASE + "/" + idOf("Água mineral"))))
                .andExpect(jsonPath("$.id").value(idOf("Água mineral").toString()))
                .andExpect(jsonPath("$.name").value("Água mineral"))
                .andExpect(jsonPath("$.unit").value("garrafa"))
                .andExpect(jsonPath("$.estimatedUnitWeightKg").value(1.5));
    }

    @Test
    void rejectsInvalidBody() throws Exception {
        create(" ", "x".repeat(31), "0").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", allOf(containsString("name:"), containsString("unit:"),
                        containsString("estimatedUnitWeightKg:"))));

        create("Arroz", "kg", "-1").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("estimatedUnitWeightKg:")));

        create("Arroz", "kg", "0.0001").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("estimatedUnitWeightKg:")));

        create("Arroz", "kg", null).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("estimatedUnitWeightKg:")));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    void getsAndListsResourcesOrderedByName() throws Exception {
        UUID feijaoId = createdId("Feijão", "kg", "1");
        UUID arrozId = createdId("Arroz", "kg", "5");

        mockMvc.perform(get(BASE + "/{id}", feijaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Feijão"));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(arrozId.toString()))
                .andExpect(jsonPath("$[1].id").value(feijaoId.toString()));
    }

    @Test
    void returnsNotFoundAndBadRequestForIds() throws Exception {
        UUID missing = UUID.randomUUID();

        mockMvc.perform(get(BASE + "/{id}", missing))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recurso " + missing + " não encontrado"));

        update(missing, "Arroz", "kg", "1").andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recurso " + missing + " não encontrado"));

        mockMvc.perform(get(BASE + "/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    void updatesAllFieldsKeepingId() throws Exception {
        UUID id = createdId("Arroz", "kg", "1");

        update(id, " Arroz integral ", " pacote ", "5.250")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Arroz integral"))
                .andExpect(jsonPath("$.unit").value("pacote"))
                .andExpect(jsonPath("$.estimatedUnitWeightKg").value(5.25));

        update(id, "", "kg", "1").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("name:")));
    }

    @Test
    void doesNotExposeDeletion() throws Exception {
        UUID id = createdId("Arroz", "kg", "1");

        mockMvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isMethodNotAllowed());
    }

    private ResultActions create(String name, String unit, String weight) throws Exception {
        return mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                .content(body(name, unit, weight)));
    }

    private ResultActions update(UUID id, String name, String unit, String weight) throws Exception {
        return mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(name, unit, weight)));
    }

    private UUID createdId(String name, String unit, String weight) throws Exception {
        String json = create(name, unit, weight).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(json).get("id").asText());
    }

    private UUID idOf(String name) {
        return repository.findAll().stream()
                .filter(resource -> resource.getName().equals(name))
                .findFirst().orElseThrow().getId();
    }

    private String body(String name, String unit, String weight) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("unit", unit);
        body.put("estimatedUnitWeightKg", weight == null ? null : new BigDecimal(weight));
        return objectMapper.writeValueAsString(body);
    }
}
