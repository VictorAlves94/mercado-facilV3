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
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService — Testes Unitários")
class DashboardServiceTest {

    @Mock VendaRepository    vendaRepository;
    @Mock DespesaRepository  despesaRepository;
    @Mock ProdutoRepository  produtoRepository;
    @Mock CaixaRepository    caixaRepository;
    @Mock LojaService        lojaService;
    @InjectMocks DashboardService dashboardService;

    private static final Long LOJA_ID = 1L;

    @Test
    @DisplayName("Resumo do dia deve calcular lucro corretamente")
    void getResumoHoje_calculaLucroCorretamente() {
        // lojaId vem do parâmetro — service usa o param direto
        when(vendaRepository.sumTotalNoPeriodo(any(), any(), eq(LOJA_ID)))
                .thenReturn(new BigDecimal("1500.00"));
        when(vendaRepository.countFinalizadasNoPeriodo(any(), any(), eq(LOJA_ID)))
                .thenReturn(25L);
        when(despesaRepository.sumTotalNoPeriodo(any(), any(), eq(LOJA_ID)))
                .thenReturn(new BigDecimal("300.00"));
        when(produtoRepository.countEstoqueBaixo(eq(LOJA_ID)))
                .thenReturn(3L);
        when(produtoRepository.countEstoqueZerado(eq(LOJA_ID)))
                .thenReturn(1L);
        when(produtoRepository.findValidadeProxima(any(), eq(LOJA_ID)))
                .thenReturn(List.of());
        when(produtoRepository.findVencidos(any(), eq(LOJA_ID)))
                .thenReturn(List.of());
        when(caixaRepository.existsByStatusAndLojaId(Caixa.StatusCaixa.ABERTO, LOJA_ID))
                .thenReturn(true);

        var resumo = dashboardService.getResumoHoje(LOJA_ID);

        assertThat(resumo.totalVendasHoje()).isEqualByComparingTo("1500.00");
        assertThat(resumo.totalDespesasHoje()).isEqualByComparingTo("300.00");
        assertThat(resumo.lucroEstimadoHoje()).isEqualByComparingTo("1200.00");
        assertThat(resumo.quantidadeVendasHoje()).isEqualTo(25L);
        assertThat(resumo.produtosEstoqueBaixo()).isEqualTo(3L);
        assertThat(resumo.caixaAberto()).isTrue();
    }

    @Test
    @DisplayName("Resumo sem vendas deve retornar valores zerados")
    void getResumoHoje_semVendas_retornaZerado() {
        when(vendaRepository.sumTotalNoPeriodo(any(), any(), eq(LOJA_ID)))
                .thenReturn(BigDecimal.ZERO);
        when(vendaRepository.countFinalizadasNoPeriodo(any(), any(), eq(LOJA_ID)))
                .thenReturn(0L);
        when(despesaRepository.sumTotalNoPeriodo(any(), any(), eq(LOJA_ID)))
                .thenReturn(BigDecimal.ZERO);
        when(produtoRepository.countEstoqueBaixo(eq(LOJA_ID)))
                .thenReturn(0L);
        when(produtoRepository.countEstoqueZerado(eq(LOJA_ID)))
                .thenReturn(0L);
        when(produtoRepository.findValidadeProxima(any(), eq(LOJA_ID)))
                .thenReturn(List.of());
        when(produtoRepository.findVencidos(any(), eq(LOJA_ID)))
                .thenReturn(List.of());
        when(caixaRepository.existsByStatusAndLojaId(Caixa.StatusCaixa.ABERTO, LOJA_ID))
                .thenReturn(false);

        var resumo = dashboardService.getResumoHoje(LOJA_ID);

        assertThat(resumo.lucroEstimadoHoje()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.caixaAberto()).isFalse();
    }

    @Test
    @DisplayName("Quando lojaId for null deve usar lojaId do usuário logado")
    void getResumoHoje_semLojaIdParam_usaLojaDoUsuario() {
        when(lojaService.getLojaIdDoUsuario()).thenReturn(LOJA_ID);
        when(vendaRepository.sumTotalNoPeriodo(any(), any(), eq(LOJA_ID)))
                .thenReturn(BigDecimal.ZERO);
        when(vendaRepository.countFinalizadasNoPeriodo(any(), any(), eq(LOJA_ID)))
                .thenReturn(0L);
        when(despesaRepository.sumTotalNoPeriodo(any(), any(), eq(LOJA_ID)))
                .thenReturn(BigDecimal.ZERO);
        when(produtoRepository.countEstoqueBaixo(eq(LOJA_ID))).thenReturn(0L);
        when(produtoRepository.countEstoqueZerado(eq(LOJA_ID))).thenReturn(0L);
        when(produtoRepository.findValidadeProxima(any(), eq(LOJA_ID))).thenReturn(List.of());
        when(produtoRepository.findVencidos(any(), eq(LOJA_ID))).thenReturn(List.of());
        when(caixaRepository.existsByStatusAndLojaId(Caixa.StatusCaixa.ABERTO, LOJA_ID))
                .thenReturn(false);

        // Passa null — service deve fazer fallback para lojaService.getLojaIdDoUsuario()
        var resumo = dashboardService.getResumoHoje(null);

        verify(lojaService).getLojaIdDoUsuario();
        assertThat(resumo).isNotNull();
    }
}