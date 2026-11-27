package com.equipe404.rotaviva.core.resources.controller;

import com.equipe404.rotaviva.core.resources.dto.ResourceRequest;
import com.equipe404.rotaviva.core.resources.dto.ResourceResponse;
import com.equipe404.rotaviva.core.resources.service.ResourceService;
import com.equipe404.rotaviva.infra.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Recursos", description = "Catálogo de recursos distribuídos")
public class ResourceController {

    private final ResourceService service;

    @PostMapping
    @Operation(operationId = "createResource", summary = "Cadastra um recurso",
            description = "Nome e unidade são salvos sem espaços nas pontas. O peso por unidade deve ser positivo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recurso criado",
                    headers = @Header(name = "Location", description = "URL do recurso criado")),
            @ApiResponse(responseCode = "400", description = "Corpo ausente, malformado ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ResourceResponse> create(@Valid @RequestBody ResourceRequest request) {
        ResourceResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(operationId = "listResources", summary = "Lista recursos ordenados por nome")
    @ApiResponse(responseCode = "200", description = "Lista de recursos")
    public List<ResourceResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getResource", summary = "Consulta um recurso por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recurso encontrado"),
            @ApiResponse(responseCode = "400", description = "ID não é um UUID válido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Recurso inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResourceResponse get(@Parameter(description = "ID do recurso") @PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateResource", summary = "Edita nome, unidade e peso de um recurso",
            description = "Substituição completa: todos os campos são obrigatórios. O ID não muda.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recurso atualizado"),
            @ApiResponse(responseCode = "400", description = "ID ou corpo inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Recurso inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResourceResponse update(@Parameter(description = "ID do recurso") @PathVariable UUID id,
                                   @Valid @RequestBody ResourceRequest request) {
        return service.update(id, request);
    }
}
