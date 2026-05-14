package com.mercadofacil.service;

import com.mercadofacil.dto.request.AlterarSenhaRequest;
import com.mercadofacil.dto.request.UsuarioRequest;
import com.mercadofacil.dto.response.UsuarioResponse;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService — Testes Unitários")
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UsuarioService usuarioService;

    private Usuario adminMock;

    @BeforeEach
    void setUp() {
        adminMock = Usuario.builder()
                .id(1L).nome("Admin").email("admin@caixabsb.com")
                .senha("$hashed$").perfil(Usuario.Perfil.ADMIN).ativo(true).build();
    }

    @Nested @DisplayName("Criar Usuário")
    class CriarUsuario {

        @Test
        @DisplayName("Deve criar usuário com dados válidos")
        void criar_dadosValidos_retornaUsuario() {
            var req = new UsuarioRequest("João Silva", "joao@caixabsb.com", "senha123", Usuario.Perfil.OPERADOR,null);
            when(usuarioRepository.existsByEmail("joao@caixabsb.com")).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("$hashed$");
            var usuarioSalvo = Usuario.builder().id(2L).nome("João Silva")
                    .email("joao@caixabsb.com").perfil(Usuario.Perfil.OPERADOR).ativo(true).build();
            when(usuarioRepository.save(any())).thenReturn(usuarioSalvo);

            UsuarioResponse resp = usuarioService.criar(req);

            assertThat(resp.nome()).isEqualTo("João Silva");
            assertThat(resp.perfil()).isEqualTo("OPERADOR");
            verify(passwordEncoder).encode("senha123");
        }

        @Test
        @DisplayName("Deve lançar exceção se email já cadastrado")
        void criar_emailDuplicado_lancaBusinessException() {
            var req = new UsuarioRequest("Outro", "admin@caixabsb.com", "123456", Usuario.Perfil.OPERADOR, null);
            when(usuarioRepository.existsByEmail("admin@caixabsb.com")).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.criar(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("admin@caixabsb.com");
        }
    }

    @Nested @DisplayName("Alterar Senha")
    class AlterarSenha {

        @BeforeEach
        void mockSecurity() {
            var auth = mock(Authentication.class);
            when(auth.getName()).thenReturn("admin@caixabsb.com");
            var ctx = mock(SecurityContext.class);
            when(ctx.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(ctx);
        }

        @Test
        @DisplayName("Deve alterar senha com senha atual correta")
        void alterarSenha_senhaCorreta_atualiza() {
            when(usuarioRepository.findByEmail("admin@caixabsb.com")).thenReturn(Optional.of(adminMock));
            when(passwordEncoder.matches("senha123", "$hashed$")).thenReturn(true);
            when(passwordEncoder.encode("novaSenha456")).thenReturn("$newhash$");
            when(usuarioRepository.save(any())).thenReturn(adminMock);

            usuarioService.alterarSenha(new AlterarSenhaRequest("senha123", "novaSenha456"));

            verify(passwordEncoder).encode("novaSenha456");
            verify(usuarioRepository).save(adminMock);
        }

        @Test
        @DisplayName("Deve lançar exceção se senha atual incorreta")
        void alterarSenha_senhaAtualErrada_lancaExcecao() {
            when(usuarioRepository.findByEmail("admin@caixabsb.com")).thenReturn(Optional.of(adminMock));
            when(passwordEncoder.matches("senhaErrada", "$hashed$")).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.alterarSenha(new AlterarSenhaRequest("senhaErrada", "nova")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Senha atual incorreta");
        }
    }

    @Nested @DisplayName("Alterar Status")
    class AlterarStatus {

        @Test
        @DisplayName("Deve desativar usuário operador normalmente")
        void alterarStatus_operador_desativa() {
            var operador = Usuario.builder().id(2L).nome("Op").email("op@test.com")
                    .perfil(Usuario.Perfil.OPERADOR).ativo(true).build();
            when(usuarioRepository.findById(2L)).thenReturn(Optional.of(operador));
            when(usuarioRepository.save(any())).thenReturn(operador);

            usuarioService.alterarStatus(2L, false);

            assertThat(operador.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("Não deve desativar último admin")
        void alterarStatus_ultimoAdmin_lancaExcecao() {
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminMock));
            when(usuarioRepository.findAll()).thenReturn(List.of(adminMock)); // só 1 admin

            assertThatThrownBy(() -> usuarioService.alterarStatus(1L, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("único administrador");
        }

        @Test
        @DisplayName("Deve lançar exceção se usuário não encontrado")
        void alterarStatus_inexistente_lancaExcecao() {
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.alterarStatus(999L, false))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
