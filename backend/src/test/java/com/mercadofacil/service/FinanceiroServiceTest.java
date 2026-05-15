package com.mercadofacil.service;

import com.mercadofacil.dto.request.DespesaRequest;
import com.mercadofacil.dto.response.DespesaResponse;
import com.mercadofacil.dto.response.RelatorioFinanceiroResponse;
import com.mercadofacil.entity.*;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FinanceiroService — Testes Unitários")
class FinanceiroServiceTest {

    @Mock DespesaRepository despesaRepository;
    @Mock TipoDespesaRepository tipoDespesaRepository;
    @Mock VendaRepository vendaRepository;
    @Mock FiadoRepository fiadoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock LojaService lojaService;
    @InjectMocks FinanceiroService financeiroService;
    @Mock AuditService auditService;

    private TipoDespesa tipoMock;
    private Usuario operadorMock;

    @BeforeEach
    void setUp() {

            ReflectionTestUtils.setField(financeiroService, "auditService", auditService);

        tipoMock = TipoDespesa.builder().id(1L).nome("Energia Elétrica").ativo(true).build();
        operadorMock = Usuario.builder().id(1L).nome("Admin").email("admin@test.com").build();

        var auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@test.com");
        var ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(operadorMock));
    }

    // ─── Lançar Despesa ───────────────────────────────────────────────────────

    @Nested @DisplayName("Lançar Despesa")
    class LancarDespesa {

        @Test
        @DisplayName("Deve lançar despesa com dados válidos")
        void lancarDespesa_dadosValidos_retornaDespesa() {
            var req = new DespesaRequest(1L, "Conta de luz de março", new BigDecimal("280.50"),
                    LocalDate.now(), Despesa.FormaPagamentoDespesa.BOLETO, null);

            Despesa despesaSalva = Despesa.builder()
                    .id(1L).tipoDespesa(tipoMock).descricao("Conta de luz de março")
                    .valor(new BigDecimal("280.50")).dataDespesa(LocalDate.now())
                    .registradoPor(operadorMock).build();

            when(tipoDespesaRepository.findById(1L)).thenReturn(Optional.of(tipoMock));
            when(despesaRepository.save(any())).thenReturn(despesaSalva);

            DespesaResponse resp = financeiroService.lancarDespesa(req);

            assertThat(resp.descricao()).isEqualTo("Conta de luz de março");
            assertThat(resp.valor()).isEqualByComparingTo("280.50");
            assertThat(resp.tipoDespesaNome()).isEqualTo("Energia Elétrica");
            verify(despesaRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tipo de despesa não existe")
        void lancarDespesa_tipoInexistente_lancaException() {
            when(tipoDespesaRepository.findById(99L)).thenReturn(Optional.empty());
            var req = new DespesaRequest(99L, "Teste", BigDecimal.TEN, LocalDate.now(), null, null);

            assertThatThrownBy(() -> financeiroService.lancarDespesa(req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── Relatório Financeiro ─────────────────────────────────────────────────

    @Nested @DisplayName("Relatório Financeiro")
    class RelatorioFinanceiro {

        @BeforeEach
        void mockRepositories() {
            when(vendaRepository.sumTotalNoPeriodo(any(), any(), any())).thenReturn(new BigDecimal("3000.00"));
            when(vendaRepository.countFinalizadasNoPeriodo(any(), any(), any())).thenReturn(50L);
            when(despesaRepository.sumTotalNoPeriodo(any(), any(), any())).thenReturn(new BigDecimal("800.00"));
            when(despesaRepository.findTotalAgrupadoPorTipo(any(), any())).thenReturn(List.of(
                    new Object[]{"Energia Elétrica", new BigDecimal("300.00"), 1L},
                    new Object[]{"Funcionário", new BigDecimal("500.00"), 1L}
            ));
            when(despesaRepository.findTotalPorDia(any(), any())).thenReturn(List.of());
            when(fiadoRepository.sumSaldoDevedorAtivo()).thenReturn(new BigDecimal("150.00"));
            when(fiadoRepository.countClientesComSaldoAtivo()).thenReturn(3L);
        }

        @Test
        @DisplayName("Deve calcular lucro líquido corretamente")
        void gerarRelatorio_calculaLucroCorreto() {
            LocalDate inicio = LocalDate.now().minusDays(7);
            LocalDate fim    = LocalDate.now().minusDays(1);

            RelatorioFinanceiroResponse rel = financeiroService.gerarRelatorio(inicio, fim);

            assertThat(rel.totalVendas()).isEqualByComparingTo("3000.00");
            assertThat(rel.totalDespesas()).isEqualByComparingTo("800.00");
            assertThat(rel.lucroLiquido()).isEqualByComparingTo("2200.00");
        }

        @Test
        @DisplayName("Deve calcular margem de lucro percentual corretamente")
        void gerarRelatorio_calculaMargemPercentual() {
            // lucro 2200 / vendas 3000 = 73.33%
            LocalDate ontem = LocalDate.now().minusDays(1);
            RelatorioFinanceiroResponse rel = financeiroService.gerarRelatorio(
                    LocalDate.now().minusDays(2), ontem);

            assertThat(rel.margemLucro()).isGreaterThan(new BigDecimal("73.00"))
                                        .isLessThan(new BigDecimal("74.00"));
        }

        @Test
        @DisplayName("Deve incluir despesas agrupadas por categoria")
        void gerarRelatorio_incluiDespesasPorCategoria() {
            LocalDate ontem = LocalDate.now().minusDays(1);
            RelatorioFinanceiroResponse rel = financeiroService.gerarRelatorio(ontem, ontem);

            assertThat(rel.despesasPorCategoria()).hasSize(2);
            assertThat(rel.despesasPorCategoria().get(0).tipoDespesa()).isEqualTo("Energia Elétrica");
        }

        @Test
        @DisplayName("Deve incluir dados de fiado em aberto")
        void gerarRelatorio_incluiFiadoEmAberto() {
            LocalDate ontem = LocalDate.now().minusDays(1);
            RelatorioFinanceiroResponse rel = financeiroService.gerarRelatorio(ontem, ontem);

            assertThat(rel.totalFiadoEmAberto()).isEqualByComparingTo("150.00");
            assertThat(rel.clientesFiadoAtivos()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Deve lançar exceção quando início é posterior ao fim")
        void gerarRelatorio_periodoInvalido_lancaBusinessException() {
            assertThatThrownBy(() -> financeiroService.gerarRelatorio(
                    LocalDate.now(), LocalDate.now().minusDays(1)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("início");
        }

        @Test
        @DisplayName("Margem deve ser zero quando não há vendas")
        void gerarRelatorio_semVendas_margemZero() {
            when(vendaRepository.sumTotalNoPeriodo(any(), any(), any())).thenReturn(BigDecimal.ZERO);
            when(vendaRepository.countFinalizadasNoPeriodo(any(), any(), any())).thenReturn(0L);

            LocalDate ontem = LocalDate.now().minusDays(1);
            RelatorioFinanceiroResponse rel = financeiroService.gerarRelatorio(ontem, ontem);

            assertThat(rel.margemLucro()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(rel.lucroLiquido()).isEqualByComparingTo(new BigDecimal("-800.00"));
        }
    }

    // ─── Saldo do Dia ─────────────────────────────────────────────────────────

    @Nested @DisplayName("Saldo do Dia")
    class SaldoDia {

        @Test
        @DisplayName("Saldo positivo quando vendas superam despesas")
        void getSaldoHoje_vendasMaiores_situacaoPositiva() {
            when(vendaRepository.sumTotalNoPeriodo(any(), any(), any())).thenReturn(new BigDecimal("1200.00"));
            when(despesaRepository.sumTotalNoPeriodo(any(), any(), any())).thenReturn(new BigDecimal("300.00"));

            var saldo = financeiroService.getSaldoHoje();

            assertThat(saldo.saldo()).isEqualByComparingTo("900.00");
            assertThat(saldo.situacao()).isEqualTo("POSITIVO");
        }

        @Test
        @DisplayName("Saldo negativo quando despesas superam vendas")
        void getSaldoHoje_despesasMaiores_situacaoNegativa() {
            when(vendaRepository.sumTotalNoPeriodo(any(), any(), any())).thenReturn(new BigDecimal("100.00"));
            when(despesaRepository.sumTotalNoPeriodo(any(), any(), any())).thenReturn(new BigDecimal("500.00"));

            var saldo = financeiroService.getSaldoHoje();

            assertThat(saldo.saldo()).isEqualByComparingTo("-400.00");
            assertThat(saldo.situacao()).isEqualTo("NEGATIVO");
        }
    }
}
