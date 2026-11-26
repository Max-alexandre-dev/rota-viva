package com.equipe404.rotaviva.core.coordinators.controller;

import com.equipe404.rotaviva.core.coordinators.dto.CoordinatorRequest;
import com.equipe404.rotaviva.core.coordinators.dto.CoordinatorResponse;
import com.equipe404.rotaviva.core.coordinators.service.CoordinatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coordinators")
@RequiredArgsConstructor
public class CoordinatorController {

    private final CoordinatorService service;

    @PostMapping
    public ResponseEntity<CoordinatorResponse> create(@Valid @RequestBody CoordinatorRequest request) {
        CoordinatorResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<CoordinatorResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public CoordinatorResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public CoordinatorResponse update(@PathVariable Long id, @Valid @RequestBody CoordinatorRequest request) {
        return service.update(id, request);
    }
}
