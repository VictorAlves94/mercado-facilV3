package com.mercadofacil.dto.response;

import com.mercadofacil.entity.ItemVenda;
import java.math.BigDecimal;

public record ItemVendaResponse(
    Long id,
    Long produtoId,
    String produtoNome,
    String produtoCodigoBarras,
    Integer quantidade,
    BigDecimal precoUnitario,
    BigDecimal desconto,
    BigDecimal subtotal,
    BigDecimal lucroItem,
    String status
) {
    public static ItemVendaResponse from(ItemVenda i) {
        return new ItemVendaResponse(
            i.getId(),
            i.getProduto().getId(),
            i.getProduto().getNome(),
            i.getProduto().getCodigoBarras(),
            i.getQuantidade(),
            i.getPrecoUnitario(),
            i.getDesconto(),
            i.getSubtotal(),
            i.getLucroItem(),
            i.getStatus().name()
        );
    }
}
