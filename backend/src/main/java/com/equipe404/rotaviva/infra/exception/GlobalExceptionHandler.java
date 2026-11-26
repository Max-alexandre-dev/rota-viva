package com.equipe404.rotaviva.infra.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice(basePackages = "com.equipe404.rotaviva.core")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException exception) {
        return businessError(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException exception) {
        return businessError(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException exception) {
        Map<String, String> details = new TreeMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                details.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "A requisição contém dados inválidos", details);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> malformedRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "Corpo ou parâmetro da requisição ausente ou inválido", Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> invalidDomainInput(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), Map.of());
    }

    private ResponseEntity<ApiError> businessError(HttpStatus status, BusinessException exception) {
        return error(status, exception.getCode(), exception.getMessage(), Map.of());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message,
                                           Map<String, String> details) {
        return ResponseEntity.status(status).body(ApiError.of(code, message, details));
    }
}
