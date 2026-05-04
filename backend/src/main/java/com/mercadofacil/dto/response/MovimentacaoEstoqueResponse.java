package com.mercadofacil.dto.response;

import com.mercadofacil.entity.MovimentacaoEstoque;

import java.time.LocalDateTime;

public record MovimentacaoEstoqueResponse(
    Long id,
    Long produtoId,
    String produtoNome,
    String tipo,
    Integer quantidade,
    Integer quantidadeAnterior,
    Integer quantidadePosterior,
    String motivo,
    String usuario,
    Long vendaId,
    LocalDateTime criadoEm
) {
    public static MovimentacaoEstoqueResponse from(MovimentacaoEstoque m) {
        return new MovimentacaoEstoqueResponse(
            m.getId(),
            m.getProduto().getId(),
            m.getProduto().getNome(),
            m.getTipo().name(),
            m.getQuantidade(),
            m.getQuantidadeAnterior(),
            m.getQuantidadePosterior(),
            m.getMotivo(),
            m.getUsuario() != null ? m.getUsuario().getNome() : null,
            m.getVenda() != null ? m.getVenda().getId() : null,
            m.getCriadoEm()
        );
    }
}
