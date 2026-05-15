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


    public DashboardResponse getResumoHoje(Long lojaId) {
        LocalDate hoje       = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim    = hoje.plusDays(1).atStartOfDay();

        // Se não veio lojaId no param, usa o do usuário logado como fallback
        Long idLoja = (lojaId != null) ? lojaId : lojaService.getLojaIdDoUsuario();

        // Vendas — agora filtradas por loja
        BigDecimal totalVendas = vendaRepository.sumTotalNoPeriodo(inicio, fim, idLoja);
        long qtdVendas         = vendaRepository.countFinalizadasNoPeriodo(inicio, fim, idLoja);
        BigDecimal ticketMedio = qtdVendas > 0
                ? totalVendas.divide(BigDecimal.valueOf(qtdVendas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Despesas — agora filtradas por loja
        BigDecimal totalDespesas = despesaRepository.sumTotalNoPeriodo(hoje, hoje, idLoja);
        BigDecimal lucroEstimado = totalVendas.subtract(totalDespesas);

        // Alertas de estoque — já tinham lojaId, só troca a variável
        long estoqueBaixo    = produtoRepository.countEstoqueBaixo(idLoja);
        long estoqueZerado   = produtoRepository.countEstoqueZerado(idLoja);
        long validadeProxima = produtoRepository.findValidadeProxima(hoje.plusDays(7), idLoja).size();
        long vencidos        = produtoRepository.findVencidos(hoje, idLoja).size();
        long totalAlertas    = estoqueBaixo + estoqueZerado + validadeProxima + vencidos;

        // Caixa — filtrado por loja
        boolean caixaAberto = caixaRepository.existsByStatusAndLojaId(
                Caixa.StatusCaixa.ABERTO, idLoja);
        CaixaResponse caixaAtual = caixaAberto
                ? caixaRepository.findByStatusAndLojaId(Caixa.StatusCaixa.ABERTO, idLoja)
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
