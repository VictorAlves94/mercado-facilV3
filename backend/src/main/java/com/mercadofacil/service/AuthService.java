package com.mercadofacil.service;

import com.mercadofacil.dto.request.LoginRequest;
import com.mercadofacil.dto.response.AuthResponse;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.repository.UsuarioRepository;
import com.mercadofacil.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        Usuario usuario = (Usuario) auth.getPrincipal();
        String token = jwtService.gerarToken(usuario);
        return AuthResponse.of(token, usuario);
    }

    public AuthResponse me(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        String token = jwtService.gerarToken(usuario);
        return AuthResponse.of(token, usuario);
    }
}
