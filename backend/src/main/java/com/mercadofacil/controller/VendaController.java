package com.mercadofacil.controller;

import com.mercadofacil.dto.request.CancelarVendaRequest;
import com.mercadofacil.dto.request.VendaRequest;
import com.mercadofacil.dto.response.PageResponse;
import com.mercadofacil.dto.response.VendaResponse;
import com.mercadofacil.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    /** Registra uma nova venda */
    @PostMapping
    public ResponseEntity<VendaResponse> registrar(@Valid @RequestBody VendaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendaService.registrar(request));
    }

    /** Busca venda por ID com todos os itens */
    @GetMapping("/{id}")
    public ResponseEntity<VendaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }

    /** Busca venda pelo número (ex: V202506010001) */
    @GetMapping("/numero/{numero}")
    public ResponseEntity<VendaResponse> buscarPorNumero(@PathVariable String numero) {
        return ResponseEntity.ok(vendaService.buscarPorNumero(numero));
    }

    /** Lista todas as vendas de um caixa (paginado) */
    @GetMapping("/caixa/{caixaId}")
    public ResponseEntity<PageResponse<VendaResponse>> listarPorCaixa(
            @PathVariable Long caixaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return ResponseEntity.ok(vendaService.listarPorCaixa(caixaId, pagina, tamanho));
    }

    /** Vendas finalizadas de hoje */
    @GetMapping("/hoje")
    public ResponseEntity<List<VendaResponse>> listarHoje() {
        return ResponseEntity.ok(vendaService.listarHoje());
    }

    /** Cancela uma venda e devolve o estoque */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<VendaResponse> cancelar(
            @PathVariable Long id,
            @Valid @RequestBody CancelarVendaRequest request) {
        return ResponseEntity.ok(vendaService.cancelar(id, request));
    }
}
