package com.mercadofacil.service;

import com.mercadofacil.dto.request.AbrirCaixaRequest;
import com.mercadofacil.dto.request.FecharCaixaRequest;
import com.mercadofacil.dto.response.CaixaResponse;
import com.mercadofacil.dto.response.ResumoFechamentoCaixaResponse;
import com.mercadofacil.entity.Caixa;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.entity.Venda;
import com.mercadofacil.exception.CaixaException;
import com.mercadofacil.repository.CaixaRepository;
import com.mercadofacil.repository.UsuarioRepository;
import com.mercadofacil.repository.VendaRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CaixaService — Testes Unitários")
class CaixaServiceTest {

    @Mock CaixaRepository caixaRepository;
    @Mock VendaRepository vendaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks CaixaService caixaService;

    @Mock
    AuditService auditService;

    private Usuario operadorMock;
    private Caixa caixaAbertoMock;

    @BeforeEach
    void setUp() {
        operadorMock = Usuario.builder()
                .id(1L).nome("Operador").email("op@test.com").build();

        caixaAbertoMock = Caixa.builder()
                .id(1L)
                .status(Caixa.StatusCaixa.ABERTO)
                .valorAbertura(new BigDecimal("100.00"))
                .totalDinheiro(new BigDecimal("500.00"))
                .totalPix(new BigDecimal("200.00"))
                .totalCartaoDebito(BigDecimal.ZERO)
                .totalCartaoCredito(BigDecimal.ZERO)
                .totalVendas(new BigDecimal("700.00"))
                .abertoPor(operadorMock)
                .abertoEm(LocalDateTime.now().minusHours(8))
                .build();

        // mock SecurityContext
        var auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("op@test.com");
        var ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        when(usuarioRepository.findByEmail("op@test.com")).thenReturn(Optional.of(operadorMock));
    }

    // ─── Abrir Caixa ──────────────────────────────────────────────────────────

    @Nested @DisplayName("Abrir Caixa")
    class AbrirCaixa {

        @Test
        @DisplayName("Deve abrir caixa com saldo inicial")
        void abrir_semCaixaAberto_abreCaixaComSucesso() {
            when(caixaRepository.existsByStatus(Caixa.StatusCaixa.ABERTO)).thenReturn(false);
            when(caixaRepository.save(any())).thenAnswer(inv -> {
                Caixa c = inv.getArgument(0);
                c = Caixa.builder().id(1L).status(c.getStatus())
                        .valorAbertura(c.getValorAbertura())
                        .totalDinheiro(BigDecimal.ZERO).totalPix(BigDecimal.ZERO)
                        .totalCartaoDebito(BigDecimal.ZERO).totalCartaoCredito(BigDecimal.ZERO)
                        .totalVendas(BigDecimal.ZERO)
                        .abertoPor(operadorMock).abertoEm(LocalDateTime.now()).build();
                return c;
            });

            CaixaResponse resp = caixaService.abrir(new AbrirCaixaRequest(new BigDecimal("150.00")));

            assertThat(resp.status()).isEqualTo("ABERTO");
            assertThat(resp.valorAbertura()).isEqualByComparingTo("150.00");
            verify(caixaRepository).save(any());
        }

        @Test
        @DisplayName("Não deve abrir se já existe caixa aberto")
        void abrir_comCaixaJaAberto_lancaCaixaException() {
            when(caixaRepository.existsByStatus(Caixa.StatusCaixa.ABERTO)).thenReturn(true);

            assertThatThrownBy(() -> caixaService.abrir(new AbrirCaixaRequest(BigDecimal.ZERO)))
                    .isInstanceOf(CaixaException.class)
                    .hasMessageContaining("Já existe um caixa aberto");
        }
    }

    // ─── Fechar Caixa ─────────────────────────────────────────────────────────

    @Nested @DisplayName("Fechar Caixa")
    class FecharCaixa {

        @Test
        @DisplayName("Deve fechar caixa e calcular resumo corretamente")
        void fechar_caixaAberto_retornaResumoCompleto() {
            when(caixaRepository.findByStatus(Caixa.StatusCaixa.ABERTO))
                    .thenReturn(Optional.of(caixaAbertoMock));
            when(vendaRepository.findFinalizadasNoPeriodo(any(), any()))
                    .thenReturn(List.of(
                            Venda.builder().id(1L).valorTotal(new BigDecimal("350.00")).build(),
                            Venda.builder().id(2L).valorTotal(new BigDecimal("350.00")).build()
                    ));
            when(caixaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var req = new FecharCaixaRequest(new BigDecimal("600.00"), "Fechamento normal");
            ResumoFechamentoCaixaResponse resumo = caixaService.fechar(req);

            assertThat(resumo.caixa().status()).isEqualTo("FECHADO");
            assertThat(resumo.quantidadeVendas()).isEqualTo(2L);
            // esperado = abertura(100) + dinheiro(500) = 600
            assertThat(resumo.totalEsperado()).isEqualByComparingTo("600.00");
            // informado = 600, diferença = 0
            assertThat(resumo.diferenca()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("Deve calcular diferença negativa quando operador informa menos que o esperado")
        void fechar_valorInformadoMenor_diferencaNegativa() {
            when(caixaRepository.findByStatus(Caixa.StatusCaixa.ABERTO))
                    .thenReturn(Optional.of(caixaAbertoMock));
            when(vendaRepository.findFinalizadasNoPeriodo(any(), any())).thenReturn(List.of());
            when(caixaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // esperado = 100 + 500 = 600, informado = 550 → diferença = -50
            var req = new FecharCaixaRequest(new BigDecimal("550.00"), null);
            ResumoFechamentoCaixaResponse resumo = caixaService.fechar(req);

            assertThat(resumo.diferenca()).isEqualByComparingTo("-50.00");
        }

        @Test
        @DisplayName("Não deve fechar se não há caixa aberto")
        void fechar_semCaixaAberto_lancaException() {
            when(caixaRepository.findByStatus(Caixa.StatusCaixa.ABERTO))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> caixaService.fechar(new FecharCaixaRequest(BigDecimal.ZERO, null)))
                    .isInstanceOf(CaixaException.class);
        }
    }

    // ─── Totais de Pagamento ──────────────────────────────────────────────────

    @Nested @DisplayName("Registrar e Estornar Pagamentos")
    class Pagamentos {

        @Test
        @DisplayName("Deve acumular total de dinheiro corretamente")
        void registrarPagamento_dinheiro_acumulaTotalDinheiro() {
            when(caixaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            caixaService.registrarPagamentoNoCaixa(
                    caixaAbertoMock, Venda.FormaPagamento.DINHEIRO, new BigDecimal("50.00"));

            assertThat(caixaAbertoMock.getTotalDinheiro()).isEqualByComparingTo("550.00");
            assertThat(caixaAbertoMock.getTotalVendas()).isEqualByComparingTo("750.00");
        }

        @Test
        @DisplayName("Deve acumular total de pix corretamente")
        void registrarPagamento_pix_acumulaTotalPix() {
            when(caixaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            caixaService.registrarPagamentoNoCaixa(
                    caixaAbertoMock, Venda.FormaPagamento.PIX, new BigDecimal("80.00"));

            assertThat(caixaAbertoMock.getTotalPix()).isEqualByComparingTo("280.00");
        }

        @Test
        @DisplayName("Estorno deve subtrair do total corretamente")
        void estornarPagamento_dinheiro_subtraiDoTotal() {
            when(caixaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            caixaService.estornarPagamentoNoCaixa(
                    caixaAbertoMock, Venda.FormaPagamento.DINHEIRO, new BigDecimal("100.00"));

            assertThat(caixaAbertoMock.getTotalDinheiro()).isEqualByComparingTo("400.00");
            assertThat(caixaAbertoMock.getTotalVendas()).isEqualByComparingTo("600.00");
        }
    }
}
