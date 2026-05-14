package com.mercadofacil.service;

import com.mercadofacil.config.LojaContext;
import com.mercadofacil.dto.request.LojaRequest;
import com.mercadofacil.dto.response.LojaResponse;
import com.mercadofacil.entity.Loja;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.LojaRepository;
import com.mercadofacil.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LojaService {

    private final LojaRepository    lojaRepository;
    private final UsuarioRepository usuarioRepository;

    public List<LojaResponse> listarAtivas() {
        return lojaRepository.findByAtivaOrderByNomeAsc(true)
                .stream().map(LojaResponse::from).toList();
    }

    public List<LojaResponse> listarTodas() {
        return lojaRepository.findAll().stream()
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .map(LojaResponse::from).toList();
    }

    public LojaResponse buscarPorId(Long id) {
        return LojaResponse.from(findOrThrow(id));
    }

    /**
     * Retorna o ID da loja da requisição atual.
     * Prioridade:
     *   1. Header X-Loja-Id (LojaContext — populado pelo LojaFilter)
     *   2. Loja vinculada ao usuário logado no banco
     *   3. null → ADMIN sem loja = acesso global
     */
    public Long getLojaIdDoUsuario() {
        // 1. Header X-Loja-Id tem prioridade
        Long lojaIdHeader = LojaContext.get();
        if (lojaIdHeader != null) return lojaIdHeader;

        // 2. Fallback: loja do usuário logado
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName() == null) return null;
            return usuarioRepository.findByEmail(auth.getName())
                    .map(Usuario::getLojaAtual)
                    .map(Loja::getId)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retorna a entidade Loja do contexto atual.
     * null → ADMIN sem loja vinculada = acesso global.
     */
    public Loja getLojaDoUsuarioLogado() {
        Long lojaId = getLojaIdDoUsuario();
        if (lojaId == null) return null;
        return lojaRepository.findById(lojaId).orElse(null);
    }

    @Transactional
    public LojaResponse criar(LojaRequest request) {
        if (lojaRepository.existsByCodigo(request.codigo())) {
            throw new BusinessException("Já existe uma loja com o código: " + request.codigo());
        }
        Loja loja = Loja.builder()
                .nome(request.nome())
                .codigo(request.codigo().toUpperCase())
                .endereco(request.endereco())
                .telefone(request.telefone())
                .cnpj(request.cnpj())
                .ativa(true)
                .build();
        Loja salva = lojaRepository.save(loja);
        log.info("Loja criada: {} ({})", salva.getNome(), salva.getCodigo());
        return LojaResponse.from(salva);
    }

    @Transactional
    public LojaResponse atualizar(Long id, LojaRequest request) {
        Loja loja = findOrThrow(id);
        lojaRepository.findByCodigo(request.codigo())
                .filter(l -> !l.getId().equals(id))
                .ifPresent(l -> { throw new BusinessException("Código já em uso por outra loja."); });
        loja.setNome(request.nome());
        loja.setCodigo(request.codigo().toUpperCase());
        loja.setEndereco(request.endereco());
        loja.setTelefone(request.telefone());
        loja.setCnpj(request.cnpj());
        return LojaResponse.from(lojaRepository.save(loja));
    }

    @Transactional
    public void alterarStatus(Long id, boolean ativa) {
        Loja loja = findOrThrow(id);
        loja.setAtiva(ativa);
        lojaRepository.save(loja);
        log.info("Loja {} {}", loja.getNome(), ativa ? "ativada" : "desativada");
    }

    public Loja findOrThrow(Long id) {
        return lojaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja", id));
    }
}