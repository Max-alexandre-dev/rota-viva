package com.equipe404.rotaviva.core.centers.controller;

import com.equipe404.rotaviva.core.centers.dto.CenterRequest;
import com.equipe404.rotaviva.core.centers.dto.CenterResponse;
import com.equipe404.rotaviva.core.centers.service.CenterService;
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
@RequestMapping("/api/v1/centers")
@RequiredArgsConstructor
@Tag(name = "Centros", description = "Cadastro de centros de apoio")
public class CenterController {

    private final CenterService service;

    @PostMapping
    @Operation(operationId = "createCenter", summary = "Cadastra um centro",
            description = "Nome e região são salvos sem espaços nas pontas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Centro criado",
                    headers = @Header(name = "Location", description = "URL do centro criado")),
            @ApiResponse(responseCode = "400", description = "Corpo ausente, malformado ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CenterResponse> create(@Valid @RequestBody CenterRequest request) {
        CenterResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(operationId = "listCenters", summary = "Lista centros ordenados por nome")
    @ApiResponse(responseCode = "200", description = "Lista de centros")
    public List<CenterResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getCenter", summary = "Consulta um centro por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Centro encontrado"),
            @ApiResponse(responseCode = "400", description = "ID não é um UUID válido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Centro inexistente (`CENTER_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CenterResponse get(@Parameter(description = "ID do centro") @PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateCenter", summary = "Edita nome e região de um centro",
            description = "Substituição completa: os dois campos são obrigatórios. ID e data de criação não mudam.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Centro atualizado"),
            @ApiResponse(responseCode = "400", description = "ID ou corpo inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Centro inexistente (`CENTER_NOT_FOUND`)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CenterResponse update(@Parameter(description = "ID do centro") @PathVariable UUID id,
                                 @Valid @RequestBody CenterRequest request) {
        return service.update(id, request);
    }
}
