package com.mercadofacil.service;

import com.mercadofacil.dto.request.DespesaRequest;
import com.mercadofacil.dto.response.*;
import com.mercadofacil.entity.Despesa;
import com.mercadofacil.entity.TipoDespesa;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceiroService {

    private final DespesaRepository despesaRepository;
    private final TipoDespesaRepository tipoDespesaRepository;
    private final VendaRepository vendaRepository;
    private final FiadoRepository fiadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;


    // ─── Tipos de Despesa ─────────────────────────────────────────────────────

    public List<TipoDespesaResponse> listarTiposDespesa() {
        return tipoDespesaRepository.findByAtivoTrueOrderByNomeAsc()
                .stream().map(TipoDespesaResponse::from).toList();
    }

    // ─── Despesas ─────────────────────────────────────────────────────────────

    public List<DespesaResponse> listarPorData(LocalDate data) {
        return despesaRepository.findByDataDespesaOrderByCriadoEmDesc(data)
                .stream().map(DespesaResponse::from).toList();
    }

    public List<DespesaResponse> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        return despesaRepository.findByDataDespesaBetweenOrderByDataDespesaDesc(inicio, fim)
                .stream().map(DespesaResponse::from).toList();
    }

    public DespesaResponse buscarPorId(Long id) {
        return DespesaResponse.from(findDespesaOrThrow(id));
    }

    @Transactional
    public DespesaResponse lancarDespesa(DespesaRequest request) {
        TipoDespesa tipo = tipoDespesaRepository.findById(request.tipoDespesaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de despesa", request.tipoDespesaId()));

        Usuario operador = getUsuarioLogado();

        Despesa despesa = Despesa.builder()
                .tipoDespesa(tipo)
                .descricao(request.descricao())
                .valor(request.valor())
                .dataDespesa(request.dataDespesa())
                .formaPagamento(request.formaPagamento())
                .observacao(request.observacao())
                .registradoPor(operador)
                .build();

        Despesa salva = despesaRepository.save(despesa);

        auditService.despesaLancada(salva.getId(), salva.getDescricao(),
                salva.getValor().toPlainString()); // ← linha nova

        log.info("💰 Despesa lançada: {} — R$ {} ({})", salva.getDescricao(), salva.getValor(), tipo.getNome());
        return DespesaResponse.from(salva);
    }

    @Transactional
    public DespesaResponse editarDespesa(Long id, DespesaRequest request) {
        Despesa despesa = findDespesaOrThrow(id);

        TipoDespesa tipo = tipoDespesaRepository.findById(request.tipoDespesaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de despesa", request.tipoDespesaId()));

        despesa.setTipoDespesa(tipo);
        despesa.setDescricao(request.descricao());
        despesa.setValor(request.valor());
        despesa.setDataDespesa(request.dataDespesa());
        despesa.setFormaPagamento(request.formaPagamento());
        despesa.setObservacao(request.observacao());

        return DespesaResponse.from(despesaRepository.save(despesa));
    }

    @Transactional
    public void excluirDespesa(Long id) {
        Despesa despesa = findDespesaOrThrow(id);

        auditService.despesaExcluida(id, despesa.getDescricao(),
                despesa.getValor().toPlainString());

        despesaRepository.delete(despesa);
        log.info("🗑️  Despesa excluída: {} (ID {})", despesa.getDescricao(), id);
    }

    // ─── Relatório Financeiro ─────────────────────────────────────────────────

    public RelatorioFinanceiroResponse gerarRelatorioHoje() {
        LocalDate hoje = LocalDate.now();
        return gerarRelatorio(hoje, hoje);
    }

    public RelatorioFinanceiroResponse gerarRelatorioMes() {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        return gerarRelatorio(inicio, LocalDate.now());
    }

    public RelatorioFinanceiroResponse gerarRelatorio(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);

        LocalDateTime dtInicio = inicio.atStartOfDay();
        LocalDateTime dtFim    = fim.plusDays(1).atStartOfDay();

        // ── Receitas ──────────────────────────────────────────────────────────
        BigDecimal totalVendas = vendaRepository.sumTotalNoPeriodo(dtInicio, dtFim);
        long qtdVendas         = vendaRepository.countFinalizadasNoPeriodo(dtInicio, dtFim);

        // ── Despesas ──────────────────────────────────────────────────────────
        BigDecimal totalDespesas = despesaRepository.sumTotalNoPeriodo(inicio, fim);

        List<RelatorioFinanceiroResponse.DespesaPorCategoria> porCategoria =
                despesaRepository.findTotalAgrupadoPorTipo(inicio, fim).stream()
                        .map(row -> new RelatorioFinanceiroResponse.DespesaPorCategoria(
                                (String) row[0],
                                (BigDecimal) row[1],
                                ((Number) row[2]).longValue()))
                        .toList();

        // ── Resultado ─────────────────────────────────────────────────────────
        BigDecimal lucroLiquido = totalVendas.subtract(totalDespesas);
        BigDecimal margemLucro  = totalVendas.compareTo(BigDecimal.ZERO) > 0
                ? lucroLiquido.divide(totalVendas, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ── Fiado ──────────────────────────────────────────────────────────────
        BigDecimal totalFiadoAberto = fiadoRepository.sumSaldoDevedorAtivo();
        long clientesFiado          = fiadoRepository.countClientesComSaldoAtivo();

        // ── Resumo diário (apenas quando período > 1 dia) ─────────────────────
        List<RelatorioFinanceiroResponse.ResumoDia> resumoDiario = List.of();
        long diasNoPeriodo = ChronoUnit.DAYS.between(inicio, fim) + 1;

        if (diasNoPeriodo > 1 && diasNoPeriodo <= 31) {
            resumoDiario = construirResumoDiario(inicio, fim);
        }

        return new RelatorioFinanceiroResponse(
                inicio, fim,
                totalVendas, qtdVendas,
                totalDespesas, porCategoria,
                lucroLiquido, margemLucro,
                totalFiadoAberto, clientesFiado,
                resumoDiario
        );
    }

    // ─── Saldo do Caixa do Dia ───────────────────────────────────────────────

    public SaldoCaixaDiaResponse getSaldoHoje() {
        LocalDate hoje   = LocalDate.now();
        LocalDateTime dt = hoje.atStartOfDay();
        LocalDateTime dtFim = hoje.plusDays(1).atStartOfDay();

        BigDecimal vendas   = vendaRepository.sumTotalNoPeriodo(dt, dtFim);
        BigDecimal despesas = despesaRepository.sumTotalNoPeriodo(hoje, hoje);
        BigDecimal saldo    = vendas.subtract(despesas);

        return new SaldoCaixaDiaResponse(hoje, vendas, despesas, saldo,
                saldo.compareTo(BigDecimal.ZERO) >= 0 ? "POSITIVO" : "NEGATIVO");
    }

    public record SaldoCaixaDiaResponse(
        LocalDate data,
        BigDecimal totalVendas,
        BigDecimal totalDespesas,
        BigDecimal saldo,
        String situacao
    ) {}

    // ─── Helpers privados ─────────────────────────────────────────────────────

    private List<RelatorioFinanceiroResponse.ResumoDia> construirResumoDiario(
            LocalDate inicio, LocalDate fim) {

        // Busca totais de despesa por dia
        Map<LocalDate, BigDecimal> despesasPorDia = despesaRepository
                .findTotalPorDia(inicio, fim).stream()
                .collect(Collectors.toMap(
                        row -> (LocalDate) row[0],
                        row -> (BigDecimal) row[1]));

        // Busca vendas para cada dia individualmente e monta a lista
        List<RelatorioFinanceiroResponse.ResumoDia> lista = new ArrayList<>();
        LocalDate cursor = inicio;
        while (!cursor.isAfter(fim)) {
            LocalDateTime dtI = cursor.atStartOfDay();
            LocalDateTime dtF = cursor.plusDays(1).atStartOfDay();
            BigDecimal venda  = vendaRepository.sumTotalNoPeriodo(dtI, dtF);
            BigDecimal desp   = despesasPorDia.getOrDefault(cursor, BigDecimal.ZERO);
            lista.add(new RelatorioFinanceiroResponse.ResumoDia(cursor, venda, desp, venda.subtract(desp)));
            cursor = cursor.plusDays(1);
        }
        return lista;
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new BusinessException("Data de início não pode ser posterior à data de fim.");
        }
    }

    private Despesa findDespesaOrThrow(Long id) {
        return despesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa", id));
    }

    private Usuario getUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado"));
    }
}
