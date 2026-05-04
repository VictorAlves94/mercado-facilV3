package com.mercadofacil.controller;

import com.mercadofacil.dto.request.FiadoRequest;
import com.mercadofacil.dto.request.LancamentoFiadoRequest;
import com.mercadofacil.dto.response.FiadoResponse;
import com.mercadofacil.dto.response.LancamentoFiadoResponse;
import com.mercadofacil.service.FiadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fiado")
@RequiredArgsConstructor
public class FiadoController {

    private final FiadoService fiadoService;

    // ─── Clientes Fiado ───────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<FiadoResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "false") boolean todos) {

        if (nome != null && !nome.isBlank()) {
            return ResponseEntity.ok(fiadoService.buscarPorNome(nome));
        }
        return ResponseEntity.ok(todos ? fiadoService.listarTodos() : fiadoService.listarAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiadoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(fiadoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<FiadoResponse> criar(@Valid @RequestBody FiadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fiadoService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FiadoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody FiadoRequest request) {
        return ResponseEntity.ok(fiadoService.atualizar(id, request));
    }

    // ─── Lançamentos ──────────────────────────────────────────────────────────

    @GetMapping("/{id}/lancamentos")
    public ResponseEntity<List<LancamentoFiadoResponse>> listarLancamentos(@PathVariable Long id) {
        return ResponseEntity.ok(fiadoService.listarLancamentos(id));
    }

    @PostMapping("/{id}/lancamentos")
    public ResponseEntity<LancamentoFiadoResponse> lancar(
            @PathVariable Long id,
            @Valid @RequestBody LancamentoFiadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fiadoService.lancar(id, request));
    }

    // ─── Status ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}/bloquear")
    public ResponseEntity<FiadoResponse> bloquear(@PathVariable Long id) {
        return ResponseEntity.ok(fiadoService.bloquear(id));
    }

    @PostMapping("/{id}/desbloquear")
    public ResponseEntity<FiadoResponse> desbloquear(@PathVariable Long id) {
        return ResponseEntity.ok(fiadoService.desbloquear(id));
    }

    @PostMapping("/{id}/quitar")
    public ResponseEntity<FiadoResponse> quitar(@PathVariable Long id) {
        return ResponseEntity.ok(fiadoService.quitar(id));
    }

    @PostMapping("/{id}/reativar")
    public ResponseEntity<FiadoResponse> reativar(@PathVariable Long id) {
        return ResponseEntity.ok(fiadoService.reativar(id));
    }
}
