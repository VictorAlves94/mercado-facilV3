package com.mercadofacil.service;

import com.mercadofacil.dto.response.CaixaResponse;
import com.mercadofacil.dto.response.DashboardResponse;
import com.mercadofacil.repository.CaixaRepository;
import com.mercadofacil.repository.DespesaRepository;
import com.mercadofacil.repository.ProdutoRepository;
import com.mercadofacil.repository.VendaRepository;
import com.mercadofacil.entity.Caixa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.mercadofacil.service.LojaService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final VendaRepository vendaRepository;
    private final DespesaRepository despesaRepository;
    private final ProdutoRepository produtoRepository;
    private final CaixaRepository caixaRepository;
    private final LojaService lojaService;

    public DashboardResponse getResumoHoje() {
        LocalDate hoje        = LocalDate.now();
        LocalDateTime inicio  = hoje.atStartOfDay();
        LocalDateTime fim     = hoje.plusDays(1).atStartOfDay();

        // Vendas
        BigDecimal totalVendas = vendaRepository.sumTotalNoPeriodo(inicio, fim);
        long qtdVendas         = vendaRepository.countFinalizadasNoPeriodo(inicio, fim);
        BigDecimal ticketMedio = qtdVendas > 0
                ? totalVendas.divide(BigDecimal.valueOf(qtdVendas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Financeiro
        BigDecimal totalDespesas  = despesaRepository.sumTotalNoPeriodo(hoje, hoje);
        BigDecimal lucroEstimado  = totalVendas.subtract(totalDespesas);

        // Alertas de estoque
        Long lojaId = lojaService.getLojaIdDoUsuario();
        long estoqueBaixo    = produtoRepository.countEstoqueBaixo(lojaId);
        long estoqueZerado   = produtoRepository.countEstoqueZerado(lojaId);
        long validadeProxima = produtoRepository.findValidadeProxima(hoje.plusDays(7), lojaId).size();
        long vencidos        = produtoRepository.findVencidos(hoje, lojaId).size();
        long totalAlertas    = estoqueBaixo + estoqueZerado + validadeProxima + vencidos;

        // Caixa atual
        boolean caixaAberto = caixaRepository.existsByStatus(Caixa.StatusCaixa.ABERTO);
        CaixaResponse caixaAtual = caixaAberto
                ? caixaRepository.findByStatus(Caixa.StatusCaixa.ABERTO)
                        .map(CaixaResponse::from).orElse(null)
                : null;

        return new DashboardResponse(
                totalVendas, qtdVendas, ticketMedio,
                totalDespesas, lucroEstimado,
                caixaAberto, caixaAtual,
                estoqueBaixo, estoqueZerado, validadeProxima, vencidos, totalAlertas
        );
    }

    // Mantido para compatibilidade com testes existentes
    public record DashboardResumo(
            BigDecimal totalVendas,
            long quantidadeVendas,
            BigDecimal totalDespesas,
            BigDecimal lucroEstimado,
            long produtosEstoqueBaixo,
            long produtosZerados,
            long produtosValidadeProxima,
            long produtosVencidos,
            boolean caixaAberto
    ) {}
}
