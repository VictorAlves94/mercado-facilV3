package com.mercadofacil.dto.response;

import com.mercadofacil.entity.Venda;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaResponse(
    Long id,
    String numeroVenda,
    Long caixaId,
    String operadorNome,
    String formaPagamento,
    String status,
    BigDecimal valorSubtotal,
    BigDecimal valorDesconto,
    BigDecimal valorTotal,
    BigDecimal valorRecebido,
    BigDecimal valorTroco,
    String motivoCancelamento,
    List<ItemVendaResponse> itens,
    LocalDateTime criadoEm,
    LocalDateTime canceladoEm
) {
    public static VendaResponse from(Venda v) {
        List<ItemVendaResponse> itensResp = v.getItens() != null
            ? v.getItens().stream().map(ItemVendaResponse::from).toList()
            : List.of();

        return new VendaResponse(
            v.getId(),
            v.getNumeroVenda(),
            v.getCaixa() != null ? v.getCaixa().getId() : null,
            v.getOperador() != null ? v.getOperador().getNome() : null,
            v.getFormaPagamento().name(),
            v.getStatus().name(),
            v.getValorSubtotal(),
            v.getValorDesconto(),
            v.getValorTotal(),
            v.getValorRecebido(),
            v.getValorTroco(),
            v.getMotivoCancelamento(),
            itensResp,
            v.getCriadoEm(),
            v.getCanceladoEm()
        );
    }
}
