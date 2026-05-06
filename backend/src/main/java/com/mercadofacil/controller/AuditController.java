package com.mercadofacil.controller;

import com.mercadofacil.dto.response.AuditLogResponse;
import com.mercadofacil.dto.response.PageResponse;
import com.mercadofacil.entity.AuditLog;
import com.mercadofacil.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auditoria")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AuditController {
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<PageResponse<AuditLogResponse>> listar(
            @RequestParam(defaultValue = "0")  int pagina,
            @RequestParam(defaultValue = "50") int tamanho,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        LocalDateTime dtInicio = inicio != null
                ? inicio.atStartOfDay()
                : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime dtFim = fim != null
                ? fim.plusDays(1).atStartOfDay()
                : LocalDate.now().plusDays(1).atStartOfDay();

        var page = auditLogRepository.buscarComFiltros(
                usuarioId, entidade, dtInicio, dtFim,
                PageRequest.of(pagina, tamanho));

        return ResponseEntity.ok(PageResponse.from(page.map(AuditLogResponse::from)));
    }

    @GetMapping("/entidade/{entidade}/{id}")
    public ResponseEntity<List<AuditLogResponse>> historicoEntidade(
            @PathVariable String entidade, @PathVariable Long id) {
        var logs = auditLogRepository
                .findByEntidadeAndEntidadeIdOrderByCriadoEmDesc(entidade, id)
                .stream().map(AuditLogResponse::from).toList();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/suspeitas")
    public ResponseEntity<List<AuditLogResponse>> acoesSuspeitas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        LocalDate dia = data != null ? data : LocalDate.now();
        var logs = auditLogRepository
                .findAcoesSuspeitasNoPeriodo(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay())
                .stream().map(AuditLogResponse::from).toList();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/cancelamentos-por-operador")
    public ResponseEntity<List<Map<String, Object>>> cancelamentosPorOperador(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        LocalDate dia = data != null ? data : LocalDate.now();
        var resultado = auditLogRepository
                .countCancelamentosPorUsuario(
                        dia.atStartOfDay(), dia.plusDays(1).atStartOfDay())
                .stream()
                .map(row -> Map.<String, Object>of(
                        "operador", row[0],
                        "totalCancelamentos", row[1]))
                .toList();
        return ResponseEntity.ok(resultado);
    }

}
