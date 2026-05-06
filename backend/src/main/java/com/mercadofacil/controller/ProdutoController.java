package com.mercadofacil.controller;

import com.mercadofacil.dto.request.AjusteEstoqueRequest;
import com.mercadofacil.dto.request.ProdutoRequest;
import com.mercadofacil.dto.response.*;
import com.mercadofacil.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    // ─── Listagem ────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<PageResponse<ProdutoResponse>> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return ResponseEntity.ok(produtoService.listar(busca, categoriaId,null, pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @GetMapping("/codigo-barras/{codigo}")
    public ResponseEntity<ProdutoResponse> buscarPorCodigoBarras(@PathVariable String codigo) {
        return produtoService.buscarPorCodigoBarras(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ProdutoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        produtoService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Estoque ─────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/estoque")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ProdutoResponse> ajustarEstoque(
            @PathVariable Long id,
            @Valid @RequestBody AjusteEstoqueRequest request) {
        return ResponseEntity.ok(produtoService.ajustarEstoque(id, request));
    }

    @GetMapping("/{id}/movimentacoes")
    public ResponseEntity<PageResponse<MovimentacaoEstoqueResponse>> listarMovimentacoes(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return ResponseEntity.ok(produtoService.listarMovimentacoes(id, pagina, tamanho));
    }

    // ─── Alertas ─────────────────────────────────────────────────────────────

    @GetMapping("/alertas")
    public ResponseEntity<AlertasEstoqueResponse> getAlertas() {
        return ResponseEntity.ok(produtoService.getAlertas());
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<ProdutoResponse>> listarEstoqueBaixo() {
        return ResponseEntity.ok(produtoService.listarEstoqueBaixo());
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<ProdutoResponse>> listarVencidos() {
        return ResponseEntity.ok(produtoService.listarVencidos());
    }

    @GetMapping("/validade-proxima")
    public ResponseEntity<List<ProdutoResponse>> listarValidadeProxima(
            @RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok(produtoService.listarValidadeProxima(dias));
    }
}
