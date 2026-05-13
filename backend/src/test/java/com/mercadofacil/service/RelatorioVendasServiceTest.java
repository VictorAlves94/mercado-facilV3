package com.mercadofacil.service;

import com.mercadofacil.dto.response.RelatorioVendasResponse;
import com.mercadofacil.entity.Venda;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.repository.VendaRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RelatorioVendasService — Testes Unitários")
class RelatorioVendasServiceTest {

    @Mock VendaRepository vendaRepository;
    @InjectMocks RelatorioVendasService relatorioService;

    @BeforeEach
    void setUp() {
        when(vendaRepository.sumTotalNoPeriodo(any(), any())).thenReturn(new BigDecimal("1500.00"));
        when(vendaRepository.countFinalizadasNoPeriodo(any(), any())).thenReturn(10L);
        when(vendaRepository.sumTotalPorFormaPagamento(any(), any(), eq(Venda.FormaPagamento.DINHEIRO))).thenReturn(new BigDecimal("600.00"));
        when(vendaRepository.sumTotalPorFormaPagamento(any(), any(), eq(Venda.FormaPagamento.PIX))).thenReturn(new BigDecimal("500.00"));
        when(vendaRepository.sumTotalPorFormaPagamento(any(), any(), eq(Venda.FormaPagamento.CARTAO_DEBITO))).thenReturn(new BigDecimal("250.00"));
        when(vendaRepository.sumTotalPorFormaPagamento(any(), any(), eq(Venda.FormaPagamento.CARTAO_CREDITO))).thenReturn(new BigDecimal("150.00"));
        when(vendaRepository.countCanceladasNoPeriodo(any(), any())).thenReturn(1L);
        when(vendaRepository.sumCanceladasNoPeriodo(any(), any())).thenReturn(new BigDecimal("29.99"));
        when(vendaRepository.findProdutosMaisVendidos(any(), any(), any(Pageable.class))).thenReturn(List.of());
    }

    @Nested @DisplayName("Relatório por período")
    class RelatorioPeriodo {

        @Test
        @DisplayName("Deve gerar relatório com totais corretos")
        void gerarRelatorioPeriodo_dadosValidos_retornaRelatorioCompleto() {
            LocalDate inicio = LocalDate.now().minusDays(7);
            LocalDate fim    = LocalDate.now().minusDays(1);

            RelatorioVendasResponse rel = relatorioService.gerarRelatorioPeriodo(inicio, fim);

            assertThat(rel.totalVendas()).isEqualByComparingTo("1500.00");
            assertThat(rel.quantidadeVendas()).isEqualTo(10L);
            assertThat(rel.ticketMedio()).isEqualByComparingTo("150.00");
            assertThat(rel.totalDinheiro()).isEqualByComparingTo("600.00");
            assertThat(rel.totalPix()).isEqualByComparingTo("500.00");
            assertThat(rel.quantidadeCanceladas()).isEqualTo(1L);
            assertThat(rel.dataInicio()).isEqualTo(inicio);
            assertThat(rel.dataFim()).isEqualTo(fim);
        }

        @Test
        @DisplayName("Deve lançar exceção quando início é posterior ao fim")
        void gerarRelatorioPeriodo_inicioAposFim_lancaBusinessException() {
            LocalDate inicio = LocalDate.now().minusDays(1);
            LocalDate fim    = LocalDate.now().minusDays(5);

            assertThatThrownBy(() -> relatorioService.gerarRelatorioPeriodo(inicio, fim))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("início");
        }

        @Test
        @DisplayName("Deve lançar exceção quando data fim é futura")
        void gerarRelatorioPeriodo_fimFuturo_lancaBusinessException() {
            LocalDate inicio = LocalDate.now();
            LocalDate fim    = LocalDate.now().plusDays(1);

            assertThatThrownBy(() -> relatorioService.gerarRelatorioPeriodo(inicio, fim))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("futura");
        }

        @Test
        @DisplayName("Ticket médio deve ser zero quando não há vendas")
        void gerarRelatorioPeriodo_semVendas_ticketMedioZero() {
            when(vendaRepository.sumTotalNoPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
            when(vendaRepository.countFinalizadasNoPeriodo(any(), any())).thenReturn(0L);

            LocalDate ontem = LocalDate.now().minusDays(1);
            RelatorioVendasResponse rel = relatorioService.gerarRelatorioPeriodo(ontem, ontem);

            assertThat(rel.ticketMedio()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(rel.quantidadeVendas()).isZero();
        }
    }

    @Nested @DisplayName("Relatórios pré-definidos")
    class RelatoriosPreDefinidos {

        @Test
        @DisplayName("Relatório de hoje não deve lançar exceção")
        void gerarRelatorioHoje_executaSemErros() {
            assertThatCode(() -> relatorioService.gerarRelatorioHoje())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Relatório da semana não deve lançar exceção")
        void gerarRelatorioSemana_executaSemErros() {
            assertThatCode(() -> relatorioService.gerarRelatorioSemana())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Relatório do mês não deve lançar exceção")
        void gerarRelatorioMes_executaSemErros() {
            assertThatCode(() -> relatorioService.gerarRelatorioMes())
                    .doesNotThrowAnyException();
        }
    }
}
