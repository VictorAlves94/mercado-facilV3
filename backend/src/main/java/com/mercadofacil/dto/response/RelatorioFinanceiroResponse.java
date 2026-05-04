package com.mercadofacil.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Relatório financeiro completo de um período.
 * Mostra receitas, despesas por categoria, lucro e saldo de fiado.
 */
public record RelatorioFinanceiroResponse(

    LocalDate dataInicio,
    LocalDate dataFim,

    // Receitas
    BigDecimal totalVendas,
    long quantidadeVendas,

    // Despesas
    BigDecimal totalDespesas,
    List<DespesaPorCategoria> despesasPorCategoria,

    // Resultado
    BigDecimal lucroLiquido,
    BigDecimal margemLucro,          // percentual

    // Fiado
    BigDecimal totalFiadoEmAberto,
    long clientesFiadoAtivos,

    // Resumo diário (quando período > 1 dia)
    List<ResumoDia> resumoDiario

) {
    public record DespesaPorCategoria(
        String tipoDespesa,
        BigDecimal total,
        long quantidade
    ) {}

    public record ResumoDia(
        LocalDate data,
        BigDecimal totalVendas,
        BigDecimal totalDespesas,
        BigDecimal lucro
    ) {}
}
