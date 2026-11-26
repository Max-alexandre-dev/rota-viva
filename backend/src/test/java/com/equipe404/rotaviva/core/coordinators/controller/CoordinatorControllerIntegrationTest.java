package com.equipe404.rotaviva.core.coordinators.controller;

import com.equipe404.rotaviva.core.coordinators.entity.Coordinator;
import com.equipe404.rotaviva.core.coordinators.repository.CoordinatorRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Usa o perfil configurado no Maven (H2 por padrao); o schema e criado pelo Hibernate (ddl-auto: update). */
@SpringBootTest
@AutoConfigureMockMvc
class CoordinatorControllerIntegrationTest {

    private static final String BASE = "/api/v1/coordinators";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CoordinatorRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void createsCoordinatorWithNormalizedEmail() throws Exception {
        create("  Ana Souza ", " Ana@Example.ORG ")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(BASE + "/" + idOf("ana@example.org"))))
                .andExpect(jsonPath("$.id").value(idOf("ana@example.org")))
                .andExpect(jsonPath("$.name").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("ana@example.org"))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    void rejectsDuplicateEmailIgnoringCaseAndSpaces() throws Exception {
        create("Ana", "ana@example.org").andExpect(status().isCreated());

        create("Outra Ana", "  ANA@example.org").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COORDINATOR_EMAIL_CONFLICT"));
    }

    @Test
    void rejectsInvalidBody() throws Exception {
        create("", "not-an-email").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.details.name", notNullValue()))
                .andExpect(jsonPath("$.details.email", notNullValue()));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void getsAndListsCoordinators() throws Exception {
        long anaId = createdId("Ana", "ana@example.org");
        long brunoId = createdId("Bruno", "bruno@example.org");

        mockMvc.perform(get(BASE + "/{id}", anaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.org"));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(anaId))
                .andExpect(jsonPath("$[1].id").value(brunoId));
    }

    @Test
    void returnsNotFoundAndBadRequestForIds() throws Exception {
        mockMvc.perform(get(BASE + "/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COORDINATOR_NOT_FOUND"));

        mockMvc.perform(put(BASE + "/{id}", 999_999).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Ana", "ana@example.org")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(BASE + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void updatesNameAndEmail() throws Exception {
        long id = createdId("Ana", "ana@example.org");

        update(id, " Ana Lima ", "ANA.LIMA@example.org")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Ana Lima"))
                .andExpect(jsonPath("$.email").value("ana.lima@example.org"));

        // Manter o proprio e-mail nao e conflito.
        update(id, "Ana L.", "ana.lima@example.org").andExpect(status().isOk());
    }

    @Test
    void rejectsUpdateToEmailOfAnotherCoordinator() throws Exception {
        createdId("Ana", "ana@example.org");
        long brunoId = createdId("Bruno", "bruno@example.org");

        update(brunoId, "Bruno", "Ana@Example.org")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COORDINATOR_EMAIL_CONFLICT"));
    }

    @Test
    void databaseEnforcesUniqueEmail() {
        repository.saveAndFlush(Coordinator.builder().name("Ana").email("ana@example.org").build());

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(Coordinator.builder().name("Outra").email("ana@example.org").build()));
    }

    private ResultActions create(String name, String email) throws Exception {
        return mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body(name, email)));
    }

    private ResultActions update(long id, String name, String email) throws Exception {
        return mockMvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(body(name, email)));
    }

    private long createdId(String name, String email) throws Exception {
        String json = create(name, email).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asLong();
    }

    private long idOf(String email) {
        return repository.findAll().stream()
                .filter(coordinator -> coordinator.getEmail().equals(email))
                .findFirst().orElseThrow().getId();
    }

    private String body(String name, String email) throws Exception {
        return objectMapper.writeValueAsString(new java.util.LinkedHashMap<>(java.util.Map.of(
                "name", name, "email", email)));
    }
}
