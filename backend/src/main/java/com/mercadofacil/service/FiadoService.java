package com.mercadofacil.service;

import com.mercadofacil.dto.request.FiadoRequest;
import com.mercadofacil.dto.request.LancamentoFiadoRequest;
import com.mercadofacil.dto.response.FiadoResponse;
import com.mercadofacil.dto.response.LancamentoFiadoResponse;
import com.mercadofacil.entity.*;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FiadoService {

    private final FiadoRepository fiadoRepository;
    private final LancamentoFiadoRepository lancamentoFiadoRepository;
    private final UsuarioRepository usuarioRepository;

    // ─── Consultas ────────────────────────────────────────────────────────────

    public List<FiadoResponse> listarAtivos() {
        return fiadoRepository.findByStatusOrderByNomeClienteAsc(Fiado.StatusFiado.ATIVO)
                .stream().map(FiadoResponse::from).toList();
    }

    public List<FiadoResponse> listarTodos() {
        return fiadoRepository.findAll().stream()
                .sorted((a, b) -> a.getNomeCliente().compareToIgnoreCase(b.getNomeCliente()))
                .map(FiadoResponse::from).toList();
    }

    public FiadoResponse buscarPorId(Long id) {
        return FiadoResponse.from(findOrThrow(id));
    }

    public List<FiadoResponse> buscarPorNome(String nome) {
        return fiadoRepository.findByNomeClienteContainingIgnoreCase(nome)
                .stream().map(FiadoResponse::from).toList();
    }

    public List<LancamentoFiadoResponse> listarLancamentos(Long fiadoId) {
        findOrThrow(fiadoId); // valida existência
        return lancamentoFiadoRepository.findByFiadoIdOrderByCriadoEmDesc(fiadoId)
                .stream().map(LancamentoFiadoResponse::from).toList();
    }

    // ─── CRUD Fiado ───────────────────────────────────────────────────────────

    @Transactional
    public FiadoResponse criar(FiadoRequest request) {
        // Não permite dois fiados ativos para o mesmo nome
        fiadoRepository.findByNomeClienteIgnoreCaseAndStatus(
                request.nomeCliente(), Fiado.StatusFiado.ATIVO)
                .ifPresent(f -> { throw new BusinessException(
                        "Já existe fiado ativo para o cliente: " + request.nomeCliente()); });

        Usuario operador = getUsuarioLogado();
        Fiado fiado = Fiado.builder()
                .nomeCliente(request.nomeCliente())
                .telefoneCliente(request.telefoneCliente())
                .saldoDevedor(BigDecimal.ZERO)
                .limiteCredito(request.limiteCredito())
                .status(Fiado.StatusFiado.ATIVO)
                .registradoPor(operador)
                .build();

        Fiado salvo = fiadoRepository.save(fiado);
        log.info("📒 Fiado criado para cliente: {} (ID {})", salvo.getNomeCliente(), salvo.getId());
        return FiadoResponse.from(salvo);
    }

    @Transactional
    public FiadoResponse atualizar(Long id, FiadoRequest request) {
        Fiado fiado = findOrThrow(id);
        fiado.setNomeCliente(request.nomeCliente());
        fiado.setTelefoneCliente(request.telefoneCliente());
        fiado.setLimiteCredito(request.limiteCredito());
        return FiadoResponse.from(fiadoRepository.save(fiado));
    }

    // ─── Lançamentos ──────────────────────────────────────────────────────────

    @Transactional
    public LancamentoFiadoResponse lancar(Long fiadoId, LancamentoFiadoRequest request) {
        Fiado fiado = findOrThrow(fiadoId);
        Usuario operador = getUsuarioLogado();

        if (fiado.getStatus() == Fiado.StatusFiado.BLOQUEADO) {
            throw new BusinessException("Fiado do cliente " + fiado.getNomeCliente() + " está bloqueado.");
        }

        switch (request.tipo()) {
            case DEBITO -> {
                // Valida limite de crédito
                if (fiado.isLimitoExcedido(request.valor())) {
                    throw new BusinessException(String.format(
                            "Lançamento de R$ %.2f excede o limite de crédito (R$ %.2f disponível).",
                            request.valor(),
                            fiado.getLimiteCredito().subtract(fiado.getSaldoDevedor())));
                }
                fiado.setSaldoDevedor(fiado.getSaldoDevedor().add(request.valor()));
                log.info("📒 Débito fiado '{}' + R$ {} → saldo: R$ {}",
                        fiado.getNomeCliente(), request.valor(), fiado.getSaldoDevedor());
            }
            case PAGAMENTO -> {
                if (request.valor().compareTo(fiado.getSaldoDevedor()) > 0) {
                    throw new BusinessException(String.format(
                            "Pagamento de R$ %.2f é maior que o saldo devedor de R$ %.2f.",
                            request.valor(), fiado.getSaldoDevedor()));
                }
                fiado.setSaldoDevedor(fiado.getSaldoDevedor().subtract(request.valor()));
                // Se zerou, quita automaticamente
                if (fiado.getSaldoDevedor().compareTo(BigDecimal.ZERO) == 0) {
                    fiado.setStatus(Fiado.StatusFiado.QUITADO);
                    log.info("✅ Fiado de '{}' QUITADO!", fiado.getNomeCliente());
                }
                log.info("💵 Pagamento fiado '{}' − R$ {} → saldo: R$ {}",
                        fiado.getNomeCliente(), request.valor(), fiado.getSaldoDevedor());
            }
        }

        fiado.setDataUltimoLancamento(LocalDate.now());
        fiadoRepository.save(fiado);

        LancamentoFiado lancamento = LancamentoFiado.builder()
                .fiado(fiado)
                .tipo(request.tipo())
                .valor(request.valor())
                .descricao(request.descricao())
                .registradoPor(operador)
                .build();

        return LancamentoFiadoResponse.from(lancamentoFiadoRepository.save(lancamento));
    }

    // ─── Gestão de Status ─────────────────────────────────────────────────────

    @Transactional
    public FiadoResponse bloquear(Long id) {
        Fiado fiado = findOrThrow(id);
        if (fiado.getStatus() == Fiado.StatusFiado.BLOQUEADO) {
            throw new BusinessException("Fiado já está bloqueado.");
        }
        fiado.setStatus(Fiado.StatusFiado.BLOQUEADO);
        log.warn("🚫 Fiado bloqueado: cliente '{}'", fiado.getNomeCliente());
        return FiadoResponse.from(fiadoRepository.save(fiado));
    }

    @Transactional
    public FiadoResponse desbloquear(Long id) {
        Fiado fiado = findOrThrow(id);
        fiado.setStatus(Fiado.StatusFiado.ATIVO);
        return FiadoResponse.from(fiadoRepository.save(fiado));
    }

    @Transactional
    public FiadoResponse quitar(Long id) {
        Fiado fiado = findOrThrow(id);
        fiado.setSaldoDevedor(BigDecimal.ZERO);
        fiado.setStatus(Fiado.StatusFiado.QUITADO);
        log.info("✅ Fiado quitado manualmente: cliente '{}'", fiado.getNomeCliente());
        return FiadoResponse.from(fiadoRepository.save(fiado));
    }

    @Transactional
    public FiadoResponse reativar(Long id) {
        Fiado fiado = findOrThrow(id);
        if (fiado.getStatus() == Fiado.StatusFiado.ATIVO) {
            throw new BusinessException("Fiado já está ativo.");
        }
        fiado.setStatus(Fiado.StatusFiado.ATIVO);
        return FiadoResponse.from(fiadoRepository.save(fiado));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Fiado findOrThrow(Long id) {
        return fiadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fiado", id));
    }

    private Usuario getUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado"));
    }
}
