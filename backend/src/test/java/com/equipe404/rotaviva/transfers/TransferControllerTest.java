package com.equipe404.rotaviva.transfers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.equipe404.rotaviva.domain.StockBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerTest {
    private static final AtomicLong OPERATION_IDS = new AtomicLong(1);

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private DemoInventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new DemoInventory();
        TransferService service = new TransferService(inventory);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new TransferController(service))
                .setControllerAdvice(new TransferApiExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createsAndGetsProposal() throws Exception {
        MvcResult created = createProposal(DemoInventory.ORIGIN_CENTER_ID,
                DemoInventory.DESTINATION_CENTER_ID, DemoInventory.RESOURCE_ID, 25);
        JsonNode response = objectMapper.readTree(created.getResponse().getContentAsString());
        String id = response.get("id").asText();

        assertEquals("DEMO_IN_MEMORY", response.get("storageMode").asText());
        mockMvc.perform(get("/api/transfers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.quantity").value(25))
                .andExpect(jsonPath("$.status").value("PROPOSTA"));
    }

    @Test
    void listsProposals() throws Exception {
        String id = transferId(createProposal(DemoInventory.ORIGIN_CENTER_ID,
                DemoInventory.DESTINATION_CENTER_ID, DemoInventory.RESOURCE_ID, 3));

        MvcResult listed = mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageMode").value("DEMO_IN_MEMORY"))
                .andReturn();

        JsonNode transfers = objectMapper.readTree(listed.getResponse().getContentAsString()).get("transfers");
        boolean found = false;
        for (JsonNode transfer : transfers) {
            if (id.equals(transfer.get("id").asText())) {
                found = true;
                assertEquals("PROPOSTA", transfer.get("status").asText());
            }
        }
        assertTrue(found, "A lista deve conter a proposta criada");
    }

    @Test
    void approvalReservesAndKeepsPhysicalStockUnchanged() throws Exception {
        StockBalance balance = inventory.findBalance(DemoInventory.ORIGIN_CENTER_ID, DemoInventory.RESOURCE_ID);
        int physicalBefore = balance.getEstoqueFisico();
        int reservedBefore = balance.getReservado();
        String id = transferId(createProposal(DemoInventory.ORIGIN_CENTER_ID,
                DemoInventory.DESTINATION_CENTER_ID, DemoInventory.RESOURCE_ID, 19));

        mockMvc.perform(post("/api/transfers/{id}/approve", id)
                        .header("Idempotency-Key", nextOperationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));

        assertEquals(physicalBefore, balance.getEstoqueFisico());
        assertEquals(reservedBefore + 19, balance.getReservado());
    }

    @Test
    void rejectionDoesNotChangeStock() throws Exception {
        StockBalance balance = inventory.findBalance(DemoInventory.ORIGIN_CENTER_ID, DemoInventory.RESOURCE_ID);
        int physicalBefore = balance.getEstoqueFisico();
        int reservedBefore = balance.getReservado();
        String id = transferId(createProposal(DemoInventory.ORIGIN_CENTER_ID,
                DemoInventory.DESTINATION_CENTER_ID, DemoInventory.RESOURCE_ID, 19));

        mockMvc.perform(post("/api/transfers/{id}/reject", id)
                        .header("Idempotency-Key", nextOperationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"));

        assertEquals(physicalBefore, balance.getEstoqueFisico());
        assertEquals(reservedBefore, balance.getReservado());
    }

    @Test
    void insufficientStockReturnsConflictWithoutPartialChanges() throws Exception {
        StockBalance balance = inventory.findBalance(DemoInventory.ORIGIN_CENTER_ID, DemoInventory.RESOURCE_ID);
        int physicalBefore = balance.getEstoqueFisico();
        int reservedBefore = balance.getReservado();
        String id = transferId(createProposal(DemoInventory.ORIGIN_CENTER_ID,
                DemoInventory.DESTINATION_CENTER_ID, DemoInventory.RESOURCE_ID, Integer.MAX_VALUE));

        mockMvc.perform(post("/api/transfers/{id}/approve", id)
                        .header("Idempotency-Key", nextOperationId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSFER_CONFLICT"));

        assertEquals(physicalBefore, balance.getEstoqueFisico());
        assertEquals(reservedBefore, balance.getReservado());
        mockMvc.perform(get("/api/transfers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROPOSTA"));
    }

    @Test
    void missingTransferReturnsNotFoundForReadAndActions() throws Exception {
        UUID missingId = UUID.fromString("90000000-0000-4000-8000-000000000001");

        mockMvc.perform(get("/api/transfers/{id}", missingId))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/transfers/{id}/approve", missingId)
                        .header("Idempotency-Key", nextOperationId()))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/transfers/{id}/reject", missingId)
                        .header("Idempotency-Key", nextOperationId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void repeatedOperationsAreIdempotentOrConflictAsSpecified() throws Exception {
        StockBalance balance = inventory.findBalance(DemoInventory.ORIGIN_CENTER_ID, DemoInventory.RESOURCE_ID);
        int reservedBefore = balance.getReservado();
        String id = transferId(createProposal(DemoInventory.ORIGIN_CENTER_ID,
                DemoInventory.DESTINATION_CENTER_ID, DemoInventory.RESOURCE_ID, 7));
        String approveKey = nextOperationId();

        mockMvc.perform(post("/api/transfers/{id}/approve", id).header("Idempotency-Key", approveKey))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/transfers/{id}/approve", id).header("Idempotency-Key", approveKey))
                .andExpect(status().isOk());
        assertEquals(reservedBefore + 7, balance.getReservado());
        mockMvc.perform(post("/api/transfers/{id}/approve", id).header("Idempotency-Key", nextOperationId()))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/transfers/{id}/reject", id).header("Idempotency-Key", nextOperationId()))
                .andExpect(status().isConflict());

        String rejectedId = transferId(createProposal(DemoInventory.ORIGIN_CENTER_ID,
                DemoInventory.DESTINATION_CENTER_ID, DemoInventory.RESOURCE_ID, 2));
        String rejectKey = nextOperationId();
        mockMvc.perform(post("/api/transfers/{id}/reject", rejectedId).header("Idempotency-Key", rejectKey))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/transfers/{id}/reject", rejectedId).header("Idempotency-Key", rejectKey))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/transfers/{id}/reject", rejectedId).header("Idempotency-Key", nextOperationId()))
                .andExpect(status().isConflict());
    }

    @Test
    void validatesBodyAndRequiresIdempotencyKey() throws Exception {
        TransferCreateRequest invalid = new TransferCreateRequest(
                DemoInventory.ORIGIN_CENTER_ID, DemoInventory.DESTINATION_CENTER_ID,
                DemoInventory.RESOURCE_ID, 0, " ");
        mockMvc.perform(post("/api/transfers")
                        .header("Idempotency-Key", nextOperationId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        TransferCreateRequest valid = request(DemoInventory.ORIGIN_CENTER_ID,
                DemoInventory.DESTINATION_CENTER_ID, DemoInventory.RESOURCE_ID, 1);
        mockMvc.perform(post("/api/transfers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(valid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reusingIdempotencyKeyWithDifferentContentReturnsConflict() throws Exception {
        String operationId = nextOperationId();
        TransferCreateRequest first = request(DemoInventory.ORIGIN_CENTER_ID,
                DemoInventory.DESTINATION_CENTER_ID, DemoInventory.RESOURCE_ID, 1);
        TransferCreateRequest different = request(DemoInventory.ORIGIN_CENTER_ID,
                DemoInventory.DESTINATION_CENTER_ID, DemoInventory.RESOURCE_ID, 2);

        mockMvc.perform(post("/api/transfers")
                        .header("Idempotency-Key", operationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(first)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/transfers")
                        .header("Idempotency-Key", operationId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(different)))
                .andExpect(status().isConflict());
    }

    @Test
    void concurrentApprovalsNeverExceedTheAvailableBalance() throws Exception {
        StockBalance balance = inventory.findBalance(DemoInventory.LIMITED_CENTER_ID, DemoInventory.RESOURCE_ID);
        int availableBefore = balance.calcularOfertavel();
        int physicalBefore = balance.getEstoqueFisico();
        int proposalCount = availableBefore + 5;
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < proposalCount; i++) {
            ids.add(transferId(createProposal(DemoInventory.LIMITED_CENTER_ID,
                    DemoInventory.DESTINATION_CENTER_ID, DemoInventory.RESOURCE_ID, 1)));
        }

        ExecutorService executor = Executors.newFixedThreadPool(proposalCount);
        CountDownLatch ready = new CountDownLatch(proposalCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> responses = new ArrayList<>();
        try {
            for (String id : ids) {
                responses.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timed out waiting to start concurrent requests");
                    }
                    return mockMvc.perform(post("/api/transfers/{id}/approve", id)
                                    .header("Idempotency-Key", nextOperationId()))
                            .andReturn().getResponse().getStatus();
                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS), "All concurrent requests should be ready");
            start.countDown();

            int approved = 0;
            int conflicts = 0;
            for (Future<Integer> response : responses) {
                int statusCode = response.get(10, TimeUnit.SECONDS);
                if (statusCode == 200) {
                    approved++;
                } else if (statusCode == 409) {
                    conflicts++;
                }
            }
            assertEquals(availableBefore, approved);
            assertEquals(proposalCount - availableBefore, conflicts);
        } finally {
            executor.shutdownNow();
        }

        assertEquals(physicalBefore, balance.getEstoqueFisico());
        assertEquals(availableBefore, balance.getReservado());
    }

    private MvcResult createProposal(UUID origin, UUID destination, UUID resource, int quantity) throws Exception {
        return mockMvc.perform(post("/api/transfers")
                        .header("Idempotency-Key", nextOperationId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request(origin, destination, resource, quantity))))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private TransferCreateRequest request(UUID origin, UUID destination, UUID resource, int quantity) {
        return new TransferCreateRequest(origin, destination, resource, quantity, "Teste demonstrativo");
    }

    private String transferId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private static String nextOperationId() {
        return new UUID(0, OPERATION_IDS.getAndIncrement()).toString();
    }
}
