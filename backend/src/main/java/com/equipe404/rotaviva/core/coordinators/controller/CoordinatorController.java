package com.equipe404.rotaviva.core.coordinators.controller;

import com.equipe404.rotaviva.core.coordinators.dto.CoordinatorRequest;
import com.equipe404.rotaviva.core.coordinators.dto.CoordinatorResponse;
import com.equipe404.rotaviva.core.coordinators.service.CoordinatorService;
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

@RestController
@RequestMapping("/api/v1/coordinators")
@RequiredArgsConstructor
@Tag(name = "Coordenadores", description = "Cadastro de coordenadores")
public class CoordinatorController {

    private final CoordinatorService service;

    @PostMapping
    @Operation(operationId = "createCoordinator", summary = "Cadastra um coordenador",
            description = "O e-mail é normalizado (sem espaços nas pontas e em minúsculas) e deve ser único.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Coordenador criado",
                    headers = @Header(name = "Location", description = "URL do coordenador criado")),
            @ApiResponse(responseCode = "400", description = "Corpo ausente, malformado ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CoordinatorResponse> create(@Valid @RequestBody CoordinatorRequest request) {
        CoordinatorResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(operationId = "listCoordinators", summary = "Lista coordenadores ordenados por ID")
    @ApiResponse(responseCode = "200", description = "Lista de coordenadores")
    public List<CoordinatorResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getCoordinator", summary = "Consulta um coordenador por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coordenador encontrado"),
            @ApiResponse(responseCode = "400", description = "ID não numérico",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Coordenador inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CoordinatorResponse get(@Parameter(description = "ID do coordenador", example = "1")
                                   @PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateCoordinator", summary = "Edita nome e e-mail de um coordenador",
            description = "Substituição completa: os dois campos são obrigatórios. O e-mail não pode pertencer a outro coordenador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coordenador atualizado"),
            @ApiResponse(responseCode = "400", description = "ID ou corpo inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Coordenador inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "E-mail de outro coordenador",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CoordinatorResponse update(@Parameter(description = "ID do coordenador", example = "1")
                                      @PathVariable Long id,
                                      @Valid @RequestBody CoordinatorRequest request) {
        return service.update(id, request);
    }
}
