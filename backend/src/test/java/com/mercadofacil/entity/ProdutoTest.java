package com.mercadofacil.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Produto Entity — Testes de regras de negócio")
class ProdutoTest {

    private Produto buildProduto(int estoque, int minimo) {
        return Produto.builder()
                .id(1L).nome("Teste")
                .quantidadeEstoque(estoque)
                .estoqueMinimo(minimo)
                .precoCusto(new BigDecimal("5.00"))
                .precoVenda(new BigDecimal("10.00"))
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("isEstoqueBaixo retorna true quando quantidade <= mínimo")
    void isEstoqueBaixo_quantidadeMenorOuIgualMinimo_retornaTrue() {
        assertThat(buildProduto(10, 10).isEstoqueBaixo()).isTrue();
        assertThat(buildProduto(5, 10).isEstoqueBaixo()).isTrue();
        assertThat(buildProduto(11, 10).isEstoqueBaixo()).isFalse();
    }

    @Test
    @DisplayName("isEstoqueZerado retorna true apenas com quantidade = 0")
    void isEstoqueZerado_quantidadeZero_retornaTrue() {
        assertThat(buildProduto(0, 10).isEstoqueZerado()).isTrue();
        assertThat(buildProduto(1, 10).isEstoqueZerado()).isFalse();
    }

    @Test
    @DisplayName("isVencido retorna true quando dataValidade é antes de hoje")
    void isVencido_dataAnterior_retornaTrue() {
        Produto p = buildProduto(10, 5);
        p.setDataValidade(LocalDate.now().minusDays(1));
        assertThat(p.isVencido()).isTrue();
    }

    @Test
    @DisplayName("isVencido retorna false quando produto sem data de validade")
    void isVencido_semDataValidade_retornaFalse() {
        Produto p = buildProduto(10, 5);
        p.setDataValidade(null);
        assertThat(p.isVencido()).isFalse();
    }

    @Test
    @DisplayName("isValidadeProxima retorna true quando vence dentro do prazo")
    void isValidadeProxima_dentroDoPrazo_retornaTrue() {
        Produto p = buildProduto(10, 5);
        p.setDataValidade(LocalDate.now().plusDays(5));
        assertThat(p.isValidadeProxima(7)).isTrue();
        assertThat(p.isValidadeProxima(3)).isFalse();
    }

    @Test
    @DisplayName("calcularMargem retorna percentual correto de lucro")
    void calcularMargem_retornaPercentualCorreto() {
        // venda 10, custo 5 → margem 50%
        Produto p = buildProduto(10, 5);
        assertThat(p.calcularMargem()).isEqualByComparingTo(new BigDecimal("50.0000"));
    }

    @Test
    @DisplayName("calcularMargem retorna zero quando preço de custo é zero")
    void calcularMargem_custZero_retornaZero() {
        Produto p = buildProduto(10, 5);
        p.setPrecoCusto(BigDecimal.ZERO);
        assertThat(p.calcularMargem()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
