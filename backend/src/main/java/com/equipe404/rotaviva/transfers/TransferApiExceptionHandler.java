package com.equipe404.rotaviva.transfers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class TransferApiExceptionHandler {
    @ExceptionHandler(TransferNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> transferNotFound(TransferNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(DemoDataNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> demoDataNotFound(DemoDataNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "DEMO_DATA_NOT_FOUND", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(TransferConflictException.class)
    public ResponseEntity<ApiErrorResponse> conflict(TransferConflictException exception) {
        return error(HttpStatus.CONFLICT, "TRANSFER_CONFLICT", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> invalidBody(MethodArgumentNotValidException exception) {
        Map<String, String> details = new TreeMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                details.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "A requisição contém dados inválidos", details);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class})
    public ResponseEntity<ApiErrorResponse> malformedRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "Corpo, UUID ou cabeçalho obrigatório ausente ou inválido", Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> invalidDomainInput(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), Map.of());
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String code, String message,
                                                   Map<String, String> details) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                code, message, details, DemoInventory.STORAGE_MODE));
    }
}
