package com.mercadofacil.service;

import com.mercadofacil.dto.request.FiadoRequest;
import com.mercadofacil.dto.request.LancamentoFiadoRequest;
import com.mercadofacil.dto.response.FiadoResponse;
import com.mercadofacil.dto.response.LancamentoFiadoResponse;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FiadoService — Testes Unitários")
class FiadoServiceTest {

    @Mock FiadoRepository fiadoRepository;
    @Mock LancamentoFiadoRepository lancamentoFiadoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks FiadoService fiadoService;

    private Usuario operadorMock;
    private Fiado fiadoAtivoMock;

    @BeforeEach
    void setUp() {
        operadorMock = Usuario.builder().id(1L).nome("Admin").email("admin@test.com").build();

        fiadoAtivoMock = Fiado.builder()
                .id(1L).nomeCliente("João Silva")
                .telefoneCliente("61999999999")
                .saldoDevedor(new BigDecimal("50.00"))
                .limiteCredito(new BigDecimal("200.00"))
                .status(Fiado.StatusFiado.ATIVO)
                .registradoPor(operadorMock).build();

        var auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@test.com");
        var ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(operadorMock));
    }

    // ─── Criar Fiado ──────────────────────────────────────────────────────────

    @Nested @DisplayName("Criar Fiado")
    class CriarFiado {

        @Test
        @DisplayName("Deve criar fiado para novo cliente")
        void criar_clienteNovo_retornaFiado() {
            when(fiadoRepository.findByNomeClienteIgnoreCaseAndStatus(anyString(), any()))
                    .thenReturn(Optional.empty());
            when(fiadoRepository.save(any())).thenReturn(fiadoAtivoMock);

            FiadoResponse resp = fiadoService.criar(
                    new FiadoRequest("João Silva", "61999999999", new BigDecimal("200.00")));

            assertThat(resp.nomeCliente()).isEqualTo("João Silva");
            assertThat(resp.status()).isEqualTo("ATIVO");
        }

        @Test
        @DisplayName("Não deve criar fiado duplicado para o mesmo cliente ativo")
        void criar_clienteDuplicado_lancaBusinessException() {
            when(fiadoRepository.findByNomeClienteIgnoreCaseAndStatus("João Silva", Fiado.StatusFiado.ATIVO))
                    .thenReturn(Optional.of(fiadoAtivoMock));

            assertThatThrownBy(() -> fiadoService.criar(
                    new FiadoRequest("João Silva", null, null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("João Silva");
        }
    }

    // ─── Lançamentos ──────────────────────────────────────────────────────────

    @Nested @DisplayName("Lançamentos de Fiado")
    class Lancamentos {

        @BeforeEach
        void mockSave() {
            when(fiadoRepository.findById(1L)).thenReturn(Optional.of(fiadoAtivoMock));
            when(fiadoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(lancamentoFiadoRepository.save(any())).thenAnswer(inv -> {
                LancamentoFiado l = inv.getArgument(0);
                return LancamentoFiado.builder().id(1L).fiado(fiadoAtivoMock)
                        .tipo(l.getTipo()).valor(l.getValor()).build();
            });
        }

        @Test
        @DisplayName("Débito deve aumentar saldo devedor")
        void lancar_debito_aumentaSaldoDevedor() {
            LancamentoFiadoResponse resp = fiadoService.lancar(1L,
                    new LancamentoFiadoRequest(LancamentoFiado.TipoLancamento.DEBITO,
                            new BigDecimal("30.00"), "Compras do dia"));

            // saldo era 50, agora deve ser 80
            assertThat(fiadoAtivoMock.getSaldoDevedor()).isEqualByComparingTo("80.00");
            assertThat(resp.tipo()).isEqualTo("DEBITO");
        }

        @Test
        @DisplayName("Pagamento deve reduzir saldo devedor")
        void lancar_pagamento_reduzSaldoDevedor() {
            fiadoService.lancar(1L,
                    new LancamentoFiadoRequest(LancamentoFiado.TipoLancamento.PAGAMENTO,
                            new BigDecimal("50.00"), "Pagamento em dinheiro"));

            assertThat(fiadoAtivoMock.getSaldoDevedor()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("Pagamento total deve quitar o fiado automaticamente")
        void lancar_pagamentoTotal_quitaFiado() {
            fiadoService.lancar(1L,
                    new LancamentoFiadoRequest(LancamentoFiado.TipoLancamento.PAGAMENTO,
                            new BigDecimal("50.00"), "Quitação"));

            assertThat(fiadoAtivoMock.getStatus()).isEqualTo(Fiado.StatusFiado.QUITADO);
        }

        @Test
        @DisplayName("Débito que excede limite deve lançar BusinessException")
        void lancar_debitoAcimaDoLimite_lancaException() {
            // saldo = 50, limite = 200, disponível = 150 → debitar 200 excede
            assertThatThrownBy(() -> fiadoService.lancar(1L,
                    new LancamentoFiadoRequest(LancamentoFiado.TipoLancamento.DEBITO,
                            new BigDecimal("200.00"), "Compra grande")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("limite");
        }

        @Test
        @DisplayName("Pagamento maior que saldo deve lançar BusinessException")
        void lancar_pagamentoMaiorQueSaldo_lancaException() {
            // saldo = 50, tentativa de pagar 100
            assertThatThrownBy(() -> fiadoService.lancar(1L,
                    new LancamentoFiadoRequest(LancamentoFiado.TipoLancamento.PAGAMENTO,
                            new BigDecimal("100.00"), "Sobrepagamento")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("saldo devedor");
        }

        @Test
        @DisplayName("Lançamento em fiado bloqueado deve lançar BusinessException")
        void lancar_fiadoBloqueado_lancaException() {
            fiadoAtivoMock.setStatus(Fiado.StatusFiado.BLOQUEADO);

            assertThatThrownBy(() -> fiadoService.lancar(1L,
                    new LancamentoFiadoRequest(LancamentoFiado.TipoLancamento.DEBITO,
                            BigDecimal.TEN, "Teste")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("bloqueado");
        }
    }

    // ─── Gestão de Status ─────────────────────────────────────────────────────

    @Nested @DisplayName("Gestão de Status do Fiado")
    class GestaoStatus {

        @BeforeEach
        void mockFind() {
            when(fiadoRepository.findById(1L)).thenReturn(Optional.of(fiadoAtivoMock));
            when(fiadoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("Bloquear deve mudar status para BLOQUEADO")
        void bloquear_fiadoAtivo_mudaStatus() {
            FiadoResponse resp = fiadoService.bloquear(1L);
            assertThat(resp.status()).isEqualTo("BLOQUEADO");
        }

        @Test
        @DisplayName("Não deve bloquear fiado já bloqueado")
        void bloquear_jaBloquedado_lancaException() {
            fiadoAtivoMock.setStatus(Fiado.StatusFiado.BLOQUEADO);
            assertThatThrownBy(() -> fiadoService.bloquear(1L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Quitar deve zerar saldo e marcar como QUITADO")
        void quitar_fiadoComSaldo_quitaEZera() {
            FiadoResponse resp = fiadoService.quitar(1L);
            assertThat(resp.status()).isEqualTo("QUITADO");
            assertThat(fiadoAtivoMock.getSaldoDevedor()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Fiado inexistente deve lançar ResourceNotFoundException")
        void bloquear_inexistente_lancaException() {
            when(fiadoRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> fiadoService.bloquear(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
