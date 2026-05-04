package com.mercadofacil.dto.response;

import com.mercadofacil.entity.Caixa;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CaixaResponse(
    Long id,
    String status,
    BigDecimal valorAbertura,
    BigDecimal valorFechamento,
    BigDecimal totalDinheiro,
    BigDecimal totalPix,
    BigDecimal totalCartaoDebito,
    BigDecimal totalCartaoCredito,
    BigDecimal totalVendas,
    BigDecimal totalGeral,
    String observacaoFechamento,
    String abertoPorNome,
    String fechadoPorNome,
    LocalDateTime abertoEm,
    LocalDateTime fechadoEm
) {
    public static CaixaResponse from(Caixa c) {
        return new CaixaResponse(
            c.getId(),
            c.getStatus().name(),
            c.getValorAbertura(),
            c.getValorFechamento(),
            c.getTotalDinheiro(),
            c.getTotalPix(),
            c.getTotalCartaoDebito(),
            c.getTotalCartaoCredito(),
            c.getTotalVendas(),
            c.getTotalGeral(),
            c.getObservacaoFechamento(),
            c.getAbertoPor() != null ? c.getAbertoPor().getNome() : null,
            c.getFechadoPor() != null ? c.getFechadoPor().getNome() : null,
            c.getAbertoEm(),
            c.getFechadoEm()
        );
    }
}
