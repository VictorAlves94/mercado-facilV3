package com.mercadofacil.service;

import com.mercadofacil.entity.Caixa;
import com.mercadofacil.repository.CaixaRepository;
import com.mercadofacil.repository.DespesaRepository;
import com.mercadofacil.repository.ProdutoRepository;
import com.mercadofacil.repository.VendaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService — Testes Unitários")
class DashboardServiceTest {

    @Mock VendaRepository vendaRepository;
    @Mock DespesaRepository despesaRepository;
    @Mock ProdutoRepository produtoRepository;
    @Mock CaixaRepository caixaRepository;
    @InjectMocks DashboardService dashboardService;

    @Test
    @DisplayName("Resumo do dia deve calcular lucro corretamente")
    void getResumoHoje_calculaLucroCorretamente() {
        when(vendaRepository.sumTotalNoPeriodo(any(), any())).thenReturn(new BigDecimal("1500.00"));
        when(vendaRepository.countFinalizadasNoPeriodo(any(), any())).thenReturn(25L);
        when(despesaRepository.sumTotalNoPeriodo(any(), any())).thenReturn(new BigDecimal("300.00"));
        when(produtoRepository.countEstoqueBaixo()).thenReturn(3L);
        when(produtoRepository.countEstoqueZerado()).thenReturn(1L);
        when(produtoRepository.findValidadeProxima(any())).thenReturn(List.of());
        when(produtoRepository.findVencidos(any())).thenReturn(List.of());
        when(caixaRepository.existsByStatus(Caixa.StatusCaixa.ABERTO)).thenReturn(true);

        var resumo = dashboardService.getResumoHoje();

        assertThat(resumo.totalVendas()).isEqualByComparingTo("1500.00");
        assertThat(resumo.totalDespesas()).isEqualByComparingTo("300.00");
        assertThat(resumo.lucroEstimado()).isEqualByComparingTo("1200.00");
        assertThat(resumo.quantidadeVendas()).isEqualTo(25L);
        assertThat(resumo.produtosEstoqueBaixo()).isEqualTo(3L);
        assertThat(resumo.caixaAberto()).isTrue();
    }

    @Test
    @DisplayName("Resumo sem vendas deve retornar valores zerados")
    void getResumoHoje_semVendas_retornaZerado() {
        when(vendaRepository.sumTotalNoPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        when(vendaRepository.countFinalizadasNoPeriodo(any(), any())).thenReturn(0L);
        when(despesaRepository.sumTotalNoPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        when(produtoRepository.countEstoqueBaixo()).thenReturn(0L);
        when(produtoRepository.countEstoqueZerado()).thenReturn(0L);
        when(produtoRepository.findValidadeProxima(any())).thenReturn(List.of());
        when(produtoRepository.findVencidos(any())).thenReturn(List.of());
        when(caixaRepository.existsByStatus(Caixa.StatusCaixa.ABERTO)).thenReturn(false);

        var resumo = dashboardService.getResumoHoje();

        assertThat(resumo.lucroEstimado()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.caixaAberto()).isFalse();
    }
}
