package com.mercadofacil.service;

import com.mercadofacil.dto.response.RelatorioVendasResponse;
import com.mercadofacil.dto.response.RelatorioVendasResponse.ProdutoMaisVendido;
import com.mercadofacil.entity.Venda;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioVendasService {

    private final VendaRepository vendaRepository;

    /**
     * Relatório para um período personalizado.
     * @param inicio data de início (inclusive)
     * @param fim    data de fim (inclusive)
     */
    public RelatorioVendasResponse gerarRelatorioPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new BusinessException("Data de início não pode ser posterior à data de fim.");
        }
        if (fim.isAfter(LocalDate.now())) {
            throw new BusinessException("Data de fim não pode ser uma data futura.");
        }

        LocalDateTime dtInicio = inicio.atStartOfDay();
        LocalDateTime dtFim    = fim.plusDays(1).atStartOfDay();

        return buildRelatorio(inicio, fim, dtInicio, dtFim);
    }

    /** Relatório do dia de hoje. */
    public RelatorioVendasResponse gerarRelatorioHoje() {
        LocalDate hoje = LocalDate.now();
        return buildRelatorio(hoje, hoje, hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay());
    }

    /** Relatório da semana corrente (dom-sáb). */
    public RelatorioVendasResponse gerarRelatorioSemana() {
        LocalDate hoje  = LocalDate.now();
        LocalDate inicio = hoje.minusDays(hoje.getDayOfWeek().getValue() % 7);
        return buildRelatorio(inicio, hoje, inicio.atStartOfDay(), hoje.plusDays(1).atStartOfDay());
    }

    /** Relatório do mês corrente. */
    public RelatorioVendasResponse gerarRelatorioMes() {
        LocalDate hoje  = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        return buildRelatorio(inicio, hoje, inicio.atStartOfDay(), hoje.plusDays(1).atStartOfDay());
    }

    // ─── Builder interno ─────────────────────────────────────────────────────

    private RelatorioVendasResponse buildRelatorio(
            LocalDate inicio, LocalDate fim,
            LocalDateTime dtInicio, LocalDateTime dtFim) {

        BigDecimal totalVendas = vendaRepository.sumTotalNoPeriodo(dtInicio, dtFim);
        long qtdVendas         = vendaRepository.countFinalizadasNoPeriodo(dtInicio, dtFim);

        BigDecimal ticketMedio = qtdVendas > 0
                ? totalVendas.divide(BigDecimal.valueOf(qtdVendas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalDinheiro  = vendaRepository.sumTotalPorFormaPagamento(dtInicio, dtFim, Venda.FormaPagamento.DINHEIRO);
        BigDecimal totalPix       = vendaRepository.sumTotalPorFormaPagamento(dtInicio, dtFim, Venda.FormaPagamento.PIX);
        BigDecimal totalDebito    = vendaRepository.sumTotalPorFormaPagamento(dtInicio, dtFim, Venda.FormaPagamento.CARTAO_DEBITO);
        BigDecimal totalCredito   = vendaRepository.sumTotalPorFormaPagamento(dtInicio, dtFim, Venda.FormaPagamento.CARTAO_CREDITO);

        long qtdCanceladas        = vendaRepository.countCanceladasNoPeriodo(dtInicio, dtFim);
        BigDecimal totalCancelado = vendaRepository.sumCanceladasNoPeriodo(dtInicio, dtFim);

        List<ProdutoMaisVendido> maisVendidos = vendaRepository
                .findProdutosMaisVendidos(dtInicio, dtFim, PageRequest.of(0, 10))
                .stream()
                .map(row -> new ProdutoMaisVendido(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .toList();

        return new RelatorioVendasResponse(
                inicio, fim,
                totalVendas, qtdVendas, ticketMedio,
                totalDinheiro, totalPix, totalDebito, totalCredito,
                qtdCanceladas, totalCancelado,
                maisVendidos
        );
    }
}
