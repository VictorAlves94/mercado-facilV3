package com.mercadofacil.dto.response;

import com.mercadofacil.entity.Usuario;

public record AuthResponse(
    String token,
    String tipo,
    Long usuarioId,
    String nome,
    String email,
    String perfil
) {
    public static AuthResponse of(String token, Usuario usuario) {
        return new AuthResponse(
            token, "Bearer",
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPerfil().name()
        );
    }
}
