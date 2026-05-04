package com.mercadofacil.controller;

import com.mercadofacil.dto.request.AlterarSenhaRequest;
import com.mercadofacil.dto.request.UsuarioRequest;
import com.mercadofacil.dto.response.UsuarioResponse;
import com.mercadofacil.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        usuarioService.alterarStatus(id, body.get("ativo"));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/minha-senha")
    public ResponseEntity<Void> alterarSenha(@Valid @RequestBody AlterarSenhaRequest request) {
        usuarioService.alterarSenha(request);
        return ResponseEntity.noContent().build();
    }
}
