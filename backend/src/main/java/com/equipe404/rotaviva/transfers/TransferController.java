package com.equipe404.rotaviva.transfers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<TransferListResponse> list() {
        return ResponseEntity.ok(new TransferListResponse(DemoInventory.STORAGE_MODE, service.list()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<TransferResponse> create(
            @RequestHeader("Idempotency-Key") UUID operationId,
            @Valid @RequestBody TransferCreateRequest request) {
        TransferResponse response = service.create(operationId, request);
        return ResponseEntity.created(URI.create("/api/transfers/" + response.id())).body(response);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<TransferResponse> approve(
            @PathVariable UUID id,
            @RequestHeader("Idempotency-Key") UUID operationId) {
        return ResponseEntity.ok(service.approve(operationId, id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<TransferResponse> reject(
            @PathVariable UUID id,
            @RequestHeader("Idempotency-Key") UUID operationId) {
        return ResponseEntity.ok(service.reject(operationId, id));
    }
}
