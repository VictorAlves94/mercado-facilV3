package com.mercadofacil.controller;

import com.mercadofacil.dto.request.AbrirCaixaRequest;
import com.mercadofacil.dto.request.FecharCaixaRequest;
import com.mercadofacil.dto.request.MovimentacaoCaixaRequest;
import com.mercadofacil.dto.response.CaixaResponse;
import com.mercadofacil.dto.response.MovimentacaoCaixaResponse;
import com.mercadofacil.dto.response.ResumoFechamentoCaixaResponse;
import com.mercadofacil.service.CaixaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/caixa")
@RequiredArgsConstructor
public class CaixaController {

    private final CaixaService caixaService;



    /** Status do caixa atual (aberto ou não) */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        boolean aberto = caixaService.hasCaixaAberto();
        if (aberto) {
            CaixaResponse caixa = caixaService.getCaixaAtual();
            return ResponseEntity.ok(Map.of("aberto", true, "caixa", caixa));
        }
        return ResponseEntity.ok(Map.of("aberto", false));
    }

    /** Retorna os dados completos do caixa aberto */
    @GetMapping("/atual")
    public ResponseEntity<CaixaResponse> getCaixaAtual() {
        return ResponseEntity.ok(caixaService.getCaixaAtual());
    }

    /** Histórico de caixas anteriores */
    @GetMapping("/historico")
    public ResponseEntity<List<CaixaResponse>> listarHistorico() {
        return ResponseEntity.ok(caixaService.listarHistorico());
    }

    /** Caixa por ID */
    @GetMapping("/{id}")
    public ResponseEntity<CaixaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(caixaService.buscarPorId(id));
    }

    /** Abre um novo caixa */
    @PostMapping("/abrir")
    public ResponseEntity<CaixaResponse> abrir(@Valid @RequestBody AbrirCaixaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caixaService.abrir(request));
    }

    /** Fecha o caixa atual e retorna o resumo completo */
    @PostMapping("/fechar")
    public ResponseEntity<ResumoFechamentoCaixaResponse> fechar(
            @Valid @RequestBody FecharCaixaRequest request) {
        return ResponseEntity.ok(caixaService.fechar(request));
    }

    /** efetuar sangria e suplementação */
    @PostMapping("/movimentacao")
    public ResponseEntity<MovimentacaoCaixaResponse> registrarMovimentacao(
            @Valid @RequestBody MovimentacaoCaixaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(caixaService.registrarMovimentacao(request));
    }

    @GetMapping("/{id}/movimentacoes")
    public ResponseEntity<List<MovimentacaoCaixaResponse>> listarMovimentacoes(
            @PathVariable Long id) {
        return ResponseEntity.ok(caixaService.listarMovimentacoes(id));
    }
}
