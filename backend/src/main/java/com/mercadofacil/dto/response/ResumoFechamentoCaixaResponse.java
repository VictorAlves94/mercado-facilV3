package com.mercadofacil.dto.response;

import java.math.BigDecimal;

/**
 * Resumo apresentado ao operador no momento do fechamento do caixa.
 * Mostra totais por forma de pagamento e diferença entre o esperado e o informado.
 */
public record ResumoFechamentoCaixaResponse(
    CaixaResponse caixa,
    BigDecimal totalEsperado,
    BigDecimal totalInformado,
    BigDecimal diferenca,
    long quantidadeVendas,
    BigDecimal ticketMedio
) {}
