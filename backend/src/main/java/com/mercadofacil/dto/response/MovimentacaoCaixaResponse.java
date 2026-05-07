package com.mercadofacil.dto.response;

import com.mercadofacil.entity.MovimentacaoCaixa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoCaixaResponse(
        Long id,
        String tipo,
        BigDecimal valor,
        String motivo,
        String operadorNome,
        LocalDateTime criadoEm

) {
    public static MovimentacaoCaixaResponse from(MovimentacaoCaixa m) {
        return new MovimentacaoCaixaResponse(
                m.getId(),
                m.getTipo().name(),
                m.getValor(),
                m.getMotivo(),
                m.getOperador() != null ? m.getOperador().getNome() : null,
                m.getCriadoEm()
        );
    }
}
