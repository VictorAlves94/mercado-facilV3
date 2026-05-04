package com.mercadofacil.controller;

import com.mercadofacil.dto.response.CategoriaResponse;
import com.mercadofacil.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<CategoriaResponse> criar(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoriaService.criar(body.get("nome"), body.get("descricao")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<CategoriaResponse> atualizar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(categoriaService.atualizar(id, body.get("nome"), body.get("descricao")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
