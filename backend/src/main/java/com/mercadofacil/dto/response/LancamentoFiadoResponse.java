package com.mercadofacil.dto.response;

import com.mercadofacil.entity.LancamentoFiado;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LancamentoFiadoResponse(
    Long id,
    Long fiadoId,
    String nomeCliente,
    String tipo,
    BigDecimal valor,
    String descricao,
    Long vendaId,
    String registradoPorNome,
    LocalDateTime criadoEm
) {
    public static LancamentoFiadoResponse from(LancamentoFiado l) {
        return new LancamentoFiadoResponse(
            l.getId(),
            l.getFiado() != null ? l.getFiado().getId() : null,
            l.getFiado() != null ? l.getFiado().getNomeCliente() : null,
            l.getTipo().name(),
            l.getValor(),
            l.getDescricao(),
            l.getVenda() != null ? l.getVenda().getId() : null,
            l.getRegistradoPor() != null ? l.getRegistradoPor().getNome() : null,
            l.getCriadoEm()
        );
    }
}
