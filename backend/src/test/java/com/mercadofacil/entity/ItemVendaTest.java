package com.mercadofacil.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ItemVenda Entity — Testes de regras de negócio")
class ItemVendaTest {

    private ItemVenda buildItem(int qtd, BigDecimal preco, BigDecimal desconto, BigDecimal custo) {
        ItemVenda item = ItemVenda.builder()
                .quantidade(qtd)
                .precoUnitario(preco)
                .desconto(desconto)
                .precoCustoSnapshot(custo)
                .status(ItemVenda.StatusItem.ATIVO)
                .build();
        item.calcularSubtotal();
        return item;
    }

    @Test
    @DisplayName("Subtotal sem desconto = preço × quantidade")
    void calcularSubtotal_semDesconto_precoVezesQuantidade() {
        ItemVenda item = buildItem(3, new BigDecimal("9.99"), BigDecimal.ZERO, new BigDecimal("6.00"));
        // 9.99 * 3 = 29.97
        assertThat(item.getSubtotal()).isEqualByComparingTo("29.97");
    }

    @Test
    @DisplayName("Subtotal com 10% de desconto aplicado corretamente")
    void calcularSubtotal_comDesconto_aplicaPercentual() {
        ItemVenda item = buildItem(2, new BigDecimal("10.00"), new BigDecimal("10.00"), new BigDecimal("6.00"));
        // 10.00 * 0.90 * 2 = 18.00
        assertThat(item.getSubtotal()).isEqualByComparingTo("18.00");
    }

    @Test
    @DisplayName("getLucroItem retorna diferença entre subtotal e custo total")
    void getLucroItem_retornaLucroCorreto() {
        // venda: 9.99 * 2 = 19.98 | custo: 6.00 * 2 = 12.00 | lucro: 7.98
        ItemVenda item = buildItem(2, new BigDecimal("9.99"), BigDecimal.ZERO, new BigDecimal("6.00"));
        assertThat(item.getLucroItem()).isEqualByComparingTo("7.98");
    }

    @Test
    @DisplayName("getLucroItem retorna zero quando sem snapshot de custo")
    void getLucroItem_semCustoSnapshot_retornaZero() {
        ItemVenda item = buildItem(1, new BigDecimal("9.99"), BigDecimal.ZERO, null);
        assertThat(item.getLucroItem()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Subtotal com desconto 100% resulta em zero")
    void calcularSubtotal_desconto100_subtotalZero() {
        ItemVenda item = buildItem(1, new BigDecimal("20.00"), new BigDecimal("100.00"), new BigDecimal("5.00"));
        assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
