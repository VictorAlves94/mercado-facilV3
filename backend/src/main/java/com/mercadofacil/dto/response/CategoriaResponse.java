package com.mercadofacil.dto.response;

import com.mercadofacil.entity.Categoria;

public record CategoriaResponse(Long id, String nome, String descricao) {
    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getDescricao());
    }
}
