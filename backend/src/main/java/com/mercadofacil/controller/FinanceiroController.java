package com.mercadofacil.controller;

import com.mercadofacil.dto.request.DespesaRequest;
import com.mercadofacil.dto.response.*;
import com.mercadofacil.service.FinanceiroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    // ─── Tipos de Despesa ─────────────────────────────────────────────────────

    @GetMapping("/tipos-despesa")
    public ResponseEntity<List<TipoDespesaResponse>> listarTiposDespesa() {
        return ResponseEntity.ok(financeiroService.listarTiposDespesa());
    }

    // ─── Despesas ─────────────────────────────────────────────────────────────

    @GetMapping("/despesas")
    public ResponseEntity<List<DespesaResponse>> listarDespesas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        if (data != null) {
            return ResponseEntity.ok(financeiroService.listarPorData(data));
        }
        if (inicio != null && fim != null) {
            return ResponseEntity.ok(financeiroService.listarPorPeriodo(inicio, fim));
        }
        // padrão: hoje
        return ResponseEntity.ok(financeiroService.listarPorData(LocalDate.now()));
    }

    @GetMapping("/despesas/{id}")
    public ResponseEntity<DespesaResponse> buscarDespesaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(financeiroService.buscarPorId(id));
    }

    @PostMapping("/despesas")
    public ResponseEntity<DespesaResponse> lancarDespesa(@Valid @RequestBody DespesaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeiroService.lancarDespesa(request));
    }

    @PutMapping("/despesas/{id}")
    public ResponseEntity<DespesaResponse> editarDespesa(
            @PathVariable Long id,
            @Valid @RequestBody DespesaRequest request) {
        return ResponseEntity.ok(financeiroService.editarDespesa(id, request));
    }

    @DeleteMapping("/despesas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> excluirDespesa(@PathVariable Long id) {
        financeiroService.excluirDespesa(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Saldo do dia ─────────────────────────────────────────────────────────

    @GetMapping("/saldo-hoje")
    public ResponseEntity<FinanceiroService.SaldoCaixaDiaResponse> getSaldoHoje() {
        return ResponseEntity.ok(financeiroService.getSaldoHoje());
    }

    // ─── Relatórios ───────────────────────────────────────────────────────────

    @GetMapping("/relatorio/hoje")
    public ResponseEntity<RelatorioFinanceiroResponse> relatorioHoje() {
        return ResponseEntity.ok(financeiroService.gerarRelatorioHoje());
    }

    @GetMapping("/relatorio/mes")
    public ResponseEntity<RelatorioFinanceiroResponse> relatorioMes() {
        return ResponseEntity.ok(financeiroService.gerarRelatorioMes());
    }

    @GetMapping("/relatorio/periodo")
    public ResponseEntity<RelatorioFinanceiroResponse> relatorioPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(financeiroService.gerarRelatorio(inicio, fim));
    }
}
