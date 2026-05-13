package com.mercadofacil.service;

import com.mercadofacil.dto.request.LoginRequest;
import com.mercadofacil.dto.response.AuthResponse;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.repository.UsuarioRepository;
import com.mercadofacil.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Testes Unitários")
class AuthServiceTest {

    @Mock AuthenticationManager authManager;
    @Mock UsuarioRepository usuarioRepository;
    @Mock JwtService jwtService;
    @InjectMocks AuthService authService;

    @Mock
    AuditService auditService;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .nome("Admin")
                .email("admin@mercadofacil.com")
                .perfil(Usuario.Perfil.ADMIN)
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Login com credenciais válidas deve retornar token")
    void login_credenciaisValidas_retornaToken() {
        var request = new LoginRequest("admin@mercadofacil.com", "admin123");
        var authToken = new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities());

        when(authManager.authenticate(any())).thenReturn(authToken);
        when(jwtService.gerarToken(usuarioMock)).thenReturn("jwt.token.aqui");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt.token.aqui");
        assertThat(response.email()).isEqualTo("admin@mercadofacil.com");
        assertThat(response.perfil()).isEqualTo("ADMIN");
        verify(jwtService).gerarToken(usuarioMock);
    }

    @Test
    @DisplayName("Login com senha errada deve lançar BadCredentialsException")
    void login_senhaErrada_lancaExcecao() {
        var request = new LoginRequest("admin@mercadofacil.com", "senhaErrada");
        when(authManager.authenticate(any())).thenThrow(BadCredentialsException.class);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Me deve retornar dados do usuário autenticado")
    void me_usuarioExistente_retornaDados() {
        when(usuarioRepository.findByEmail("admin@mercadofacil.com")).thenReturn(Optional.of(usuarioMock));
        when(jwtService.gerarToken(usuarioMock)).thenReturn("jwt.novo.token");

        AuthResponse response = authService.me("admin@mercadofacil.com");

        assertThat(response.nome()).isEqualTo("Admin");
        assertThat(response.tipo()).isEqualTo("Bearer");
    }
}
