package com.mercadofacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Relatório completo de vendas de um período.
 * Usado no Dashboard e na tela de Relatórios do front-end.
 */
public record RelatorioVendasResponse(

    // Período
    LocalDate dataInicio,
    LocalDate dataFim,

    // Totais gerais
    BigDecimal totalVendas,
    long quantidadeVendas,
    BigDecimal ticketMedio,

    // Por forma de pagamento
    BigDecimal totalDinheiro,
    BigDecimal totalPix,
    BigDecimal totalCartaoDebito,
    BigDecimal totalCartaoCredito,

    // Cancelamentos
    long quantidadeCanceladas,
    BigDecimal totalCancelado,

    // Produtos mais vendidos
    List<ProdutoMaisVendido> produtosMaisVendidos

) {
    public record ProdutoMaisVendido(
        Long produtoId,
        String produtoNome,
        long quantidadeVendida
    ) {}
}
