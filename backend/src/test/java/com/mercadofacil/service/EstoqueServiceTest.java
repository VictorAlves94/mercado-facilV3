package com.mercadofacil.service;

import com.mercadofacil.entity.*;
import com.mercadofacil.exception.EstoqueInsuficienteException;
import com.mercadofacil.repository.MovimentacaoEstoqueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EstoqueService — Testes Unitários")
class EstoqueServiceTest {

    @Mock MovimentacaoEstoqueRepository movimentacaoRepository;
    @InjectMocks EstoqueService estoqueService;

    private Produto produto;
    private Usuario operador;
    private Venda venda;

    @BeforeEach
    void setUp() {
        produto = Produto.builder()
                .id(1L).nome("Arroz 5kg")
                .quantidadeEstoque(30).estoqueMinimo(10)
                .precoCusto(new BigDecimal("18.00"))
                .precoVenda(new BigDecimal("27.90"))
                .ativo(true).build();

        operador = Usuario.builder().id(1L).nome("Operador").build();
        venda = Venda.builder().id(1L).numeroVenda("V-00001").build();
        when(movimentacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Baixa por venda deve reduzir estoque corretamente")
    void baixarEstoquePorVenda_reduzEstoque() {
        estoqueService.baixarEstoquePorVenda(produto, 5, venda, operador);

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(25);
    }

    @Test
    @DisplayName("Baixa por venda deve registrar movimentação SAIDA_VENDA")
    void baixarEstoquePorVenda_registraMovimentacaoCorreta() {
        var captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);

        estoqueService.baixarEstoquePorVenda(produto, 5, venda, operador);

        verify(movimentacaoRepository).save(captor.capture());
        MovimentacaoEstoque mov = captor.getValue();
        assertThat(mov.getTipo()).isEqualTo(MovimentacaoEstoque.TipoMovimentacao.SAIDA_VENDA);
        assertThat(mov.getQuantidade()).isEqualTo(5);
        assertThat(mov.getQuantidadeAnterior()).isEqualTo(30);
        assertThat(mov.getQuantidadePosterior()).isEqualTo(25);
        assertThat(mov.getMotivo()).contains("V-00001");
    }

    @Test
    @DisplayName("Baixa com estoque insuficiente deve lançar EstoqueInsuficienteException")
    void baixarEstoquePorVenda_insuficiente_lancaException() {
        assertThatThrownBy(() ->
            estoqueService.baixarEstoquePorVenda(produto, 100, venda, operador))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("Arroz 5kg")
                .hasMessageContaining("30")
                .hasMessageContaining("100");
    }

    @Test
    @DisplayName("Entrada deve aumentar estoque corretamente")
    void entradaEstoque_aumentaEstoque() {
        var captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);

        estoqueService.entradaEstoque(produto, 20, "Reposição fornecedor", operador);

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(50);
        verify(movimentacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(MovimentacaoEstoque.TipoMovimentacao.ENTRADA);
        assertThat(captor.getValue().getQuantidadePosterior()).isEqualTo(50);
    }

    @Test
    @DisplayName("Devolução deve restaurar estoque")
    void devolverEstoque_restauraEstoque() {
        estoqueService.devolverEstoque(produto, 3, venda, operador);

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(33);
        var captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(MovimentacaoEstoque.TipoMovimentacao.DEVOLUCAO);
    }

    @Test
    @DisplayName("Ajuste de inventário deve definir novo valor absoluto")
    void ajustarEstoque_defineNovoValorAbsoluto() {
        estoqueService.ajustarEstoque(produto, 50, "Inventário mensal", operador);

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(50);
    }

    @Test
    @DisplayName("Baixa com quantidade zerada não deve alterar estoque zerado")
    void baixarEstoquePorVenda_estoqueZerado_lancaException() {
        produto.setQuantidadeEstoque(0);

        assertThatThrownBy(() ->
            estoqueService.baixarEstoquePorVenda(produto, 1, venda, operador))
                .isInstanceOf(EstoqueInsuficienteException.class);
    }
}
