package com.mercadofacil.dto.response;

import com.mercadofacil.entity.Fiado;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FiadoResponse(
    Long id,
    String nomeCliente,
    String telefoneCliente,
    BigDecimal saldoDevedor,
    BigDecimal limiteCredito,
    String status,
    LocalDate dataUltimoLancamento,
    String registradoPorNome,
    LocalDateTime criadoEm
) {
    public static FiadoResponse from(Fiado f) {
        return new FiadoResponse(
            f.getId(),
            f.getNomeCliente(),
            f.getTelefoneCliente(),
            f.getSaldoDevedor(),
            f.getLimiteCredito(),
            f.getStatus().name(),
            f.getDataUltimoLancamento(),
            f.getRegistradoPor() != null ? f.getRegistradoPor().getNome() : null,
            f.getCriadoEm()
        );
    }
}
