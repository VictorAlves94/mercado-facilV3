package com.mercadofacil.dto.response;

import java.math.BigDecimal;

/**
 * Payload completo do dashboard.
 * Inclui dados do caixa atual, resumo do dia e alertas.
 */
public record DashboardResponse(

    // Vendas do dia
    BigDecimal totalVendasHoje,
    long quantidadeVendasHoje,
    BigDecimal ticketMedioHoje,

    // Financeiro do dia
    BigDecimal totalDespesasHoje,
    BigDecimal lucroEstimadoHoje,

    // Caixa
    boolean caixaAberto,
    CaixaResponse caixaAtual,

    // Alertas de estoque
    long produtosEstoqueBaixo,
    long produtosEstoqueZerado,
    long produtosValidadeProxima,
    long produtosVencidos,
    long totalAlertas
) {}
