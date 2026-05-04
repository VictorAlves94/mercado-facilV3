package com.mercadofacil.dto.response;

import com.mercadofacil.entity.TipoDespesa;

public record TipoDespesaResponse(Long id, String nome, String descricao, boolean ativo) {
    public static TipoDespesaResponse from(TipoDespesa t) {
        return new TipoDespesaResponse(t.getId(), t.getNome(), t.getDescricao(), t.isAtivo());
    }
}
