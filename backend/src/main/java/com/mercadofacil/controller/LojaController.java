package com.mercadofacil.controller;

import com.mercadofacil.dto.request.LojaRequest;
import com.mercadofacil.dto.response.LojaResponse;
import com.mercadofacil.service.LojaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lojas")
@RequiredArgsConstructor
public class LojaController {
    private final LojaService lojaService;

    @GetMapping
    public ResponseEntity<List<LojaResponse>> listar(
            @RequestParam(defaultValue = "false") boolean todas) {
        return ResponseEntity.ok(
                todas ? lojaService.listarTodas() : lojaService.listarAtivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LojaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(lojaService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LojaResponse> criar(
            @Valid @RequestBody LojaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lojaService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LojaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody LojaRequest request) {
        return ResponseEntity.ok(lojaService.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        lojaService.alterarStatus(id, body.get("ativa"));
        return ResponseEntity.noContent().build();
    }
}
