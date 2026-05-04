package com.mercadofacil.service;

import com.mercadofacil.dto.request.AlterarSenhaRequest;
import com.mercadofacil.dto.request.UsuarioRequest;
import com.mercadofacil.dto.response.UsuarioResponse;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::from).toList();
    }

    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.from(findOrThrow(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Já existe um usuário com o email: " + request.email());
        }
        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .ativo(true)
                .build();
        Usuario salvo = usuarioRepository.save(usuario);
        log.info("✅ Usuário criado: {} ({})", salvo.getNome(), salvo.getEmail());
        return UsuarioResponse.from(salvo);
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = findOrThrow(id);
        // verifica email duplicado em outro usuário
        usuarioRepository.findByEmail(request.email())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new BusinessException("Email já em uso por outro usuário."); });

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setPerfil(request.perfil());
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public void alterarSenha(AlterarSenhaRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new BusinessException("Senha atual incorreta.");
        }
        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);
        log.info("🔑 Senha alterada para usuário: {}", email);
    }

    @Transactional
    public void alterarStatus(Long id, boolean ativo) {
        Usuario usuario = findOrThrow(id);
        // Protege o último admin
        if (!ativo && usuario.getPerfil() == Usuario.Perfil.ADMIN) {
            long totalAdmins = usuarioRepository.findAll().stream()
                    .filter(u -> u.getPerfil() == Usuario.Perfil.ADMIN && u.isAtivo()).count();
            if (totalAdmins <= 1) {
                throw new BusinessException("Não é possível desativar o único administrador do sistema.");
            }
        }
        usuario.setAtivo(ativo);
        usuarioRepository.save(usuario);
        log.info("👤 Usuário {} {}", usuario.getEmail(), ativo ? "ativado" : "desativado");
    }

    private Usuario findOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }
}
