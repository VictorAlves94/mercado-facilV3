package com.mercadofacil.dto.response;

import com.mercadofacil.entity.Loja;
import java.time.LocalDateTime;

public record LojaResponse(
        Long id,
        String nome,
        String codigo,
        String endereco,
        String telefone,
        String cnpj,
        boolean ativa,
        LocalDateTime criadoEm
) {
    public static LojaResponse from(Loja l) {
        return new LojaResponse(
                l.getId(), l.getNome(), l.getCodigo(),
                l.getEndereco(), l.getTelefone(), l.getCnpj(),
                l.isAtiva(), l.getCriadoEm()
        );
    }
}
