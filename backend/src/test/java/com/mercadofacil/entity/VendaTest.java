package com.mercadofacil.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Venda Entity — Testes de regras de negócio")
class VendaTest {

    private Venda novaVenda() {
        return Venda.builder()
                .valorDesconto(BigDecimal.ZERO)
                .itens(new ArrayList<>())
                .build();
    }

    private ItemVenda novoItem(BigDecimal preco, int qtd) {
        ItemVenda item = ItemVenda.builder()
                .precoUnitario(preco)
                .quantidade(qtd)
                .desconto(BigDecimal.ZERO)
                .status(ItemVenda.StatusItem.ATIVO)
                .build();
        item.calcularSubtotal();
        return item;
    }

    @Test
    @DisplayName("calcularTotais soma apenas itens ATIVO")
    void calcularTotais_somaApenasItensAtivos() {
        Venda venda = novaVenda();
        ItemVenda ativo    = novoItem(new BigDecimal("10.00"), 2);  // 20.00
        ItemVenda cancelado = novoItem(new BigDecimal("5.00"),  1);  // 5.00 — deve ser ignorado
        cancelado.setStatus(ItemVenda.StatusItem.CANCELADO);

        venda.addItem(ativo);
        venda.addItem(cancelado);
        venda.calcularTotais();

        assertThat(venda.getValorSubtotal()).isEqualByComparingTo("20.00");
        assertThat(venda.getValorTotal()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("calcularTotais aplica desconto geral no total")
    void calcularTotais_aplicaDescontoGeral() {
        Venda venda = novaVenda();
        venda.setValorDesconto(new BigDecimal("5.00"));
        venda.addItem(novoItem(new BigDecimal("10.00"), 3)); // 30.00

        venda.calcularTotais();

        assertThat(venda.getValorSubtotal()).isEqualByComparingTo("30.00");
        assertThat(venda.getValorTotal()).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("addItem deve associar venda ao item")
    void addItem_associaVendaAoItem() {
        Venda venda = novaVenda();
        ItemVenda item = novoItem(BigDecimal.TEN, 1);

        venda.addItem(item);

        assertThat(item.getVenda()).isSameAs(venda);
        assertThat(venda.getItens()).hasSize(1);
    }

    @Test
    @DisplayName("Venda sem itens tem subtotal zero")
    void calcularTotais_semItens_subtotalZero() {
        Venda venda = novaVenda();
        venda.calcularTotais();

        assertThat(venda.getValorSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(venda.getValorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
