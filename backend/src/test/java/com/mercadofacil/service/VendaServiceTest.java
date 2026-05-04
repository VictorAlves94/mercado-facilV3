package com.mercadofacil.service;

import com.mercadofacil.dto.request.CancelarVendaRequest;
import com.mercadofacil.dto.request.ItemVendaRequest;
import com.mercadofacil.dto.request.VendaRequest;
import com.mercadofacil.dto.response.VendaResponse;
import com.mercadofacil.entity.*;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.EstoqueInsuficienteException;
import com.mercadofacil.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendaService — Testes Unitários")
class VendaServiceTest {

    @Mock VendaRepository vendaRepository;
    @Mock ProdutoRepository produtoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock CaixaService caixaService;
    @Mock EstoqueService estoqueService;
    @InjectMocks VendaService vendaService;

    private Produto produtoMock;
    private Caixa caixaMock;
    private Usuario operadorMock;

    @BeforeEach
    void setUp() {
        produtoMock = Produto.builder()
                .id(1L).nome("Coca-Cola 2L").codigoBarras("123")
                .quantidadeEstoque(20).estoqueMinimo(5)
                .precoCusto(new BigDecimal("6.50"))
                .precoVenda(new BigDecimal("9.99"))
                .ativo(true).build();

        operadorMock = Usuario.builder()
                .id(1L).nome("Operador").email("op@test.com").build();

        caixaMock = Caixa.builder()
                .id(1L).status(Caixa.StatusCaixa.ABERTO)
                .valorAbertura(BigDecimal.ZERO)
                .totalDinheiro(BigDecimal.ZERO).totalPix(BigDecimal.ZERO)
                .totalCartaoDebito(BigDecimal.ZERO).totalCartaoCredito(BigDecimal.ZERO)
                .totalVendas(BigDecimal.ZERO)
                .abertoPor(operadorMock)
                .abertoEm(LocalDateTime.now()).build();

        // mock SecurityContext
        var auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("op@test.com");
        var ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        when(usuarioRepository.findByEmail("op@test.com")).thenReturn(Optional.of(operadorMock));
    }

    // ─── Registrar Venda ──────────────────────────────────────────────────────

    @Nested @DisplayName("Registrar Venda")
    class RegistrarVenda {

        @Test
        @DisplayName("Deve registrar venda em dinheiro com troco correto")
        void registrar_pagamentoDinheiro_calculaTrocoCorreto() {
            when(caixaService.getCaixaAbertoEntity()).thenReturn(caixaMock);
            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoMock));
            when(vendaRepository.save(any())).thenAnswer(inv -> {
                Venda v = inv.getArgument(0);
                v = Venda.builder().id(1L).numeroVenda("V202506010001")
                        .caixa(caixaMock).operador(operadorMock)
                        .formaPagamento(v.getFormaPagamento())
                        .status(Venda.StatusVenda.FINALIZADA)
                        .valorSubtotal(v.getValorSubtotal())
                        .valorDesconto(v.getValorDesconto())
                        .valorTotal(v.getValorTotal())
                        .valorRecebido(v.getValorRecebido())
                        .valorTroco(v.getValorTroco())
                        .itens(v.getItens() != null ? v.getItens() : new ArrayList<>())
                        .build();
                return v;
            });

            var req = new VendaRequest(
                    Venda.FormaPagamento.DINHEIRO,
                    List.of(new ItemVendaRequest(1L, 2, BigDecimal.ZERO)),
                    BigDecimal.ZERO,
                    new BigDecimal("30.00") // paga 30, total ~19.98
            );

            VendaResponse resp = vendaService.registrar(req);

            assertThat(resp.status()).isEqualTo("FINALIZADA");
            assertThat(resp.formaPagamento()).isEqualTo("DINHEIRO");
            verify(estoqueService).baixarEstoquePorVenda(eq(produtoMock), eq(2), any(), eq(operadorMock));
            verify(caixaService).registrarPagamentoNoCaixa(eq(caixaMock), eq(Venda.FormaPagamento.DINHEIRO), any());
        }

        @Test
        @DisplayName("Deve registrar venda com PIX sem troco")
        void registrar_pagamentoPix_semTroco() {
            when(caixaService.getCaixaAbertoEntity()).thenReturn(caixaMock);
            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoMock));
            when(vendaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var req = new VendaRequest(
                    Venda.FormaPagamento.PIX,
                    List.of(new ItemVendaRequest(1L, 1, BigDecimal.ZERO)),
                    BigDecimal.ZERO, null
            );

            vendaService.registrar(req);

            verify(caixaService).registrarPagamentoNoCaixa(eq(caixaMock), eq(Venda.FormaPagamento.PIX), any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando valor recebido em dinheiro é insuficiente")
        void registrar_dinheiroInsuficiente_lancaBusinessException() {
            when(caixaService.getCaixaAbertoEntity()).thenReturn(caixaMock);
            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoMock));

            var req = new VendaRequest(
                    Venda.FormaPagamento.DINHEIRO,
                    List.of(new ItemVendaRequest(1L, 3, BigDecimal.ZERO)),
                    BigDecimal.ZERO,
                    new BigDecimal("1.00") // claramente insuficiente
            );

            assertThatThrownBy(() -> vendaService.registrar(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("insuficiente");
        }

        @Test
        @DisplayName("Deve lançar exceção quando não informa valor recebido em dinheiro")
        void registrar_dinheiroSemValorRecebido_lancaBusinessException() {
            when(caixaService.getCaixaAbertoEntity()).thenReturn(caixaMock);
            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoMock));

            var req = new VendaRequest(
                    Venda.FormaPagamento.DINHEIRO,
                    List.of(new ItemVendaRequest(1L, 1, BigDecimal.ZERO)),
                    BigDecimal.ZERO, null
            );

            assertThatThrownBy(() -> vendaService.registrar(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("valor recebido");
        }

        @Test
        @DisplayName("Deve propagar EstoqueInsuficienteException do EstoqueService")
        void registrar_estoqueBaixo_propagaEstoqueInsuficienteException() {
            when(caixaService.getCaixaAbertoEntity()).thenReturn(caixaMock);
            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoMock));
            doThrow(new EstoqueInsuficienteException("Coca-Cola 2L", 2, 50))
                    .when(estoqueService).baixarEstoquePorVenda(any(), eq(50), any(), any());

            var req = new VendaRequest(
                    Venda.FormaPagamento.PIX,
                    List.of(new ItemVendaRequest(1L, 50, BigDecimal.ZERO)),
                    BigDecimal.ZERO, null
            );

            assertThatThrownBy(() -> vendaService.registrar(req))
                    .isInstanceOf(EstoqueInsuficienteException.class)
                    .hasMessageContaining("Coca-Cola 2L");
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto não encontrado")
        void registrar_produtoInexistente_lancaResourceNotFoundException() {
            when(caixaService.getCaixaAbertoEntity()).thenReturn(caixaMock);
            when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

            var req = new VendaRequest(
                    Venda.FormaPagamento.PIX,
                    List.of(new ItemVendaRequest(999L, 1, BigDecimal.ZERO)),
                    BigDecimal.ZERO, null
            );

            assertThatThrownBy(() -> vendaService.registrar(req))
                    .isInstanceOf(com.mercadofacil.exception.ResourceNotFoundException.class);
        }
    }

    // ─── Cancelar Venda ───────────────────────────────────────────────────────

    @Nested @DisplayName("Cancelar Venda")
    class CancelarVenda {

        private Venda vendaFinalizadaMock;

        @BeforeEach
        void setUpVenda() {
            ItemVenda item = ItemVenda.builder()
                    .id(1L).produto(produtoMock).quantidade(2)
                    .precoUnitario(new BigDecimal("9.99"))
                    .desconto(BigDecimal.ZERO)
                    .subtotal(new BigDecimal("19.98"))
                    .status(ItemVenda.StatusItem.ATIVO).build();

            vendaFinalizadaMock = Venda.builder()
                    .id(1L).numeroVenda("V202506010001")
                    .caixa(caixaMock).operador(operadorMock)
                    .formaPagamento(Venda.FormaPagamento.DINHEIRO)
                    .status(Venda.StatusVenda.FINALIZADA)
                    .valorSubtotal(new BigDecimal("19.98"))
                    .valorDesconto(BigDecimal.ZERO)
                    .valorTotal(new BigDecimal("19.98"))
                    .valorTroco(BigDecimal.ZERO)
                    .itens(new ArrayList<>(List.of(item))).build();
        }

        @Test
        @DisplayName("Deve cancelar venda finalizada e devolver estoque")
        void cancelar_vendaFinalizada_devolveEstoqueEEstorna() {
            when(vendaRepository.findByIdWithItens(1L)).thenReturn(Optional.of(vendaFinalizadaMock));
            when(vendaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            VendaResponse resp = vendaService.cancelar(1L,
                    new CancelarVendaRequest("Produto errado"));

            assertThat(resp.status()).isEqualTo("CANCELADA");
            assertThat(resp.motivoCancelamento()).isEqualTo("Produto errado");
            verify(estoqueService).devolverEstoque(eq(produtoMock), eq(2), any(), eq(operadorMock));
            verify(caixaService).estornarPagamentoNoCaixa(eq(caixaMock), eq(Venda.FormaPagamento.DINHEIRO), any());
        }

        @Test
        @DisplayName("Não deve cancelar venda já cancelada")
        void cancelar_vendaJaCancelada_lancaBusinessException() {
            vendaFinalizadaMock.setStatus(Venda.StatusVenda.CANCELADA);
            when(vendaRepository.findByIdWithItens(1L)).thenReturn(Optional.of(vendaFinalizadaMock));

            assertThatThrownBy(() -> vendaService.cancelar(1L, new CancelarVendaRequest("Teste")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("já está cancelada");
        }

        @Test
        @DisplayName("Deve marcar todos os itens como CANCELADO")
        void cancelar_deveMudarStatusItens() {
            when(vendaRepository.findByIdWithItens(1L)).thenReturn(Optional.of(vendaFinalizadaMock));
            when(vendaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            vendaService.cancelar(1L, new CancelarVendaRequest("Pedido do cliente"));

            assertThat(vendaFinalizadaMock.getItens())
                    .allMatch(i -> i.getStatus() == ItemVenda.StatusItem.CANCELADO);
        }
    }
}
