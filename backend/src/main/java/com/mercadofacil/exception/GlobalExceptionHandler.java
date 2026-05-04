package com.mercadofacil.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(buildError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            erros.put(field, error.getDefaultMessage());
        });
        var body = new ValidationErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Erro de validação", request.getRequestURI(), erros);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError(HttpStatus.UNAUTHORIZED, "Email ou senha incorretos", request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(HttpStatus.FORBIDDEN, "Acesso negado: permissão insuficiente", request.getRequestURI()));
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleEstoqueInsuficiente(EstoqueInsuficienteException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(CaixaException.class)
    public ResponseEntity<ErrorResponse> handleCaixa(CaixaException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro interno do servidor. Tente novamente.", request.getRequestURI()));
    }

    private ErrorResponse buildError(HttpStatus status, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), message, path);
    }

    public record ErrorResponse(LocalDateTime timestamp, int status, String message, String path) {}
    public record ValidationErrorResponse(LocalDateTime timestamp, int status, String message, String path, Map<String, String> erros) {}
}
