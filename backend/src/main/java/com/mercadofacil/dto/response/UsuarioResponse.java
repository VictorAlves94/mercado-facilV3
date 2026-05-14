package com.mercadofacil.dto.response;

import com.mercadofacil.entity.Usuario;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Long          id,
        String        nome,
        String        email,
        String        perfil,
        boolean       ativo,
        LocalDateTime criadoEm,
        Long          lojaId,    // ← ADICIONAR
        String        lojaNome   // ← ADICIONAR
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getPerfil().name(),
                u.isAtivo(),
                u.getCriadoEm(),
                u.getLojaAtual() != null ? u.getLojaAtual().getId()   : null,  // ← ADICIONAR
                u.getLojaAtual() != null ? u.getLojaAtual().getNome() : null   // ← ADICIONAR
        );
    }
}