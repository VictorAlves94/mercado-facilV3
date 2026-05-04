package com.mercadofacil.dto.response;

import com.mercadofacil.entity.Despesa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DespesaResponse(
    Long id,
    Long tipoDespesaId,
    String tipoDespesaNome,
    String descricao,
    BigDecimal valor,
    LocalDate dataDespesa,
    String formaPagamento,
    String observacao,
    String registradoPorNome,
    LocalDateTime criadoEm
) {
    public static DespesaResponse from(Despesa d) {
        return new DespesaResponse(
            d.getId(),
            d.getTipoDespesa() != null ? d.getTipoDespesa().getId() : null,
            d.getTipoDespesa() != null ? d.getTipoDespesa().getNome() : null,
            d.getDescricao(),
            d.getValor(),
            d.getDataDespesa(),
            d.getFormaPagamento() != null ? d.getFormaPagamento().name() : null,
            d.getObservacao(),
            d.getRegistradoPor() != null ? d.getRegistradoPor().getNome() : null,
            d.getCriadoEm()
        );
    }
}
