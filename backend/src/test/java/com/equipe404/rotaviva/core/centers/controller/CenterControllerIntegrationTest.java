package com.equipe404.rotaviva.core.centers.controller;

import com.equipe404.rotaviva.core.centers.repository.CenterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Usa o perfil configurado no Maven (H2 por padrao); o schema e criado pelo Hibernate (ddl-auto: update). */
@SpringBootTest
@AutoConfigureMockMvc
class CenterControllerIntegrationTest {

    private static final String BASE = "/api/v1/centers";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CenterRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void createsCenterWithStrippedFields() throws Exception {
        create("  Centro Norte ", " Zona Norte ")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(BASE + "/" + idOf("Centro Norte"))))
                .andExpect(jsonPath("$.id").value(idOf("Centro Norte").toString()))
                .andExpect(jsonPath("$.name").value("Centro Norte"))
                .andExpect(jsonPath("$.region").value("Zona Norte"))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void rejectsInvalidBody() throws Exception {
        create(" ", "x".repeat(121)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.details.name", notNullValue()))
                .andExpect(jsonPath("$.details.region", notNullValue()));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void getsAndListsCentersOrderedByName() throws Exception {
        UUID sulId = createdId("Centro Sul", "Zona Sul");
        UUID norteId = createdId("Centro Norte", "Zona Norte");

        mockMvc.perform(get(BASE + "/{id}", sulId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("Zona Sul"));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(norteId.toString()))
                .andExpect(jsonPath("$[1].id").value(sulId.toString()));
    }

    @Test
    void returnsNotFoundAndBadRequestForIds() throws Exception {
        UUID missing = UUID.randomUUID();

        mockMvc.perform(get(BASE + "/{id}", missing))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CENTER_NOT_FOUND"));

        update(missing, "Centro", "Zona").andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CENTER_NOT_FOUND"));

        mockMvc.perform(get(BASE + "/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void updatesNameAndRegionKeepingIdAndCreation() throws Exception {
        String created = create("Centro Norte", "Zona Norte").andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(objectMapper.readTree(created).get("id").asText());
        String createdAt = objectMapper.readTree(created).get("createdAt").asText();

        update(id, " Centro Leste ", "Zona Leste")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Centro Leste"))
                .andExpect(jsonPath("$.region").value("Zona Leste"))
                .andExpect(jsonPath("$.createdAt").value(createdAt));

        update(id, "", "Zona").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name", notNullValue()));
    }

    @Test
    void doesNotExposeDeletion() throws Exception {
        UUID id = createdId("Centro Norte", "Zona Norte");

        mockMvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isMethodNotAllowed());
    }

    private ResultActions create(String name, String region) throws Exception {
        return mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body(name, region)));
    }

    private ResultActions update(UUID id, String name, String region) throws Exception {
        return mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(name, region)));
    }

    private UUID createdId(String name, String region) throws Exception {
        String json = create(name, region).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(json).get("id").asText());
    }

    private UUID idOf(String name) {
        return repository.findAll().stream()
                .filter(center -> center.getName().equals(name))
                .findFirst().orElseThrow().getId();
    }

    private String body(String name, String region) throws Exception {
        return objectMapper.writeValueAsString(new LinkedHashMap<>(Map.of("name", name, "region", region)));
    }
}
