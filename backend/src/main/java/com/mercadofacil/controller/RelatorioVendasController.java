package com.mercadofacil.controller;

import com.mercadofacil.dto.response.RelatorioVendasResponse;
import com.mercadofacil.service.RelatorioVendasService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/relatorios/vendas")
@RequiredArgsConstructor
public class RelatorioVendasController {

    private final RelatorioVendasService relatorioService;

    /** Relatório do dia atual */
    @GetMapping("/hoje")
    public ResponseEntity<RelatorioVendasResponse> hoje() {
        return ResponseEntity.ok(relatorioService.gerarRelatorioHoje());
    }

    /** Relatório da semana corrente */
    @GetMapping("/semana")
    public ResponseEntity<RelatorioVendasResponse> semana() {
        return ResponseEntity.ok(relatorioService.gerarRelatorioSemana());
    }

    /** Relatório do mês corrente */
    @GetMapping("/mes")
    public ResponseEntity<RelatorioVendasResponse> mes() {
        return ResponseEntity.ok(relatorioService.gerarRelatorioMes());
    }

    /**
     * Relatório por período personalizado.
     * Ex: GET /relatorios/vendas/periodo?inicio=2025-01-01&fim=2025-01-31
     */
    @GetMapping("/periodo")
    public ResponseEntity<RelatorioVendasResponse> periodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.gerarRelatorioPeriodo(inicio, fim));
    }
}
