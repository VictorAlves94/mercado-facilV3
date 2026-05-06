package com.mercadofacil.dto.response;
import com.mercadofacil.entity.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String usuarioNome,
        String acao,
        String descricao,
        String entidade,
        Long entidadeId,
        String entidadeReferencia,
        String valorAnterior,
        String valorPosterior,
        LocalDateTime criadoEm
) {
    public static AuditLogResponse from(AuditLog a) {
        return new AuditLogResponse(
                a.getId(), a.getUsuarioNome(), a.getAcao().name(),
                a.getDescricao(), a.getEntidade(), a.getEntidadeId(),
                a.getEntidadeReferencia(), a.getValorAnterior(),
                a.getValorPosterior(), a.getCriadoEm()
        );
    }
}
