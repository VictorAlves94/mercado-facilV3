package com.mercadofacil.service;

import com.mercadofacil.dto.request.AbrirCaixaRequest;
import com.mercadofacil.dto.request.FecharCaixaRequest;
import com.mercadofacil.dto.request.MovimentacaoCaixaRequest;
import com.mercadofacil.dto.response.CaixaResponse;
import com.mercadofacil.dto.response.MovimentacaoCaixaResponse;
import com.mercadofacil.dto.response.ResumoFechamentoCaixaResponse;
import com.mercadofacil.entity.Caixa;
import com.mercadofacil.entity.MovimentacaoCaixa;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.entity.Venda;
import com.mercadofacil.exception.CaixaException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.CaixaRepository;
import com.mercadofacil.repository.MovimentacaoCaixaRepository;
import com.mercadofacil.repository.UsuarioRepository;
import com.mercadofacil.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaixaService {

    private final CaixaRepository caixaRepository;
    private final VendaRepository vendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;
    private final MovimentacaoCaixaRepository movimentacaoCaixaRepository;
    private final LojaService lojaService;



    // ─── Consultas ────────────────────────────────────────────────────────────

    public CaixaResponse getCaixaAtual() {
        Long lojaId = lojaService.getLojaIdDoUsuario();
        return caixaRepository.findByStatusAndLojaId(Caixa.StatusCaixa.ABERTO, lojaId)
                .map(CaixaResponse::from)
                .orElseThrow(CaixaException::semCaixaAberto);
    }

    public boolean hasCaixaAberto() {
        Long lojaId = lojaService.getLojaIdDoUsuario();
        return caixaRepository.existsByStatusAndLojaId(Caixa.StatusCaixa.ABERTO, lojaId);
    }
    public CaixaResponse buscarPorId(Long id) {
        return CaixaResponse.from(findOrThrow(id));
    }

    public List<CaixaResponse> listarHistorico() {
        return caixaRepository.findAllOrderByAbertoemDesc()
                .stream().map(CaixaResponse::from).toList();
    }

    // ─── Abrir Caixa ─────────────────────────────────────────────────────────

    @Transactional
    public CaixaResponse abrir(AbrirCaixaRequest request) {
        if (caixaRepository.existsByStatus(Caixa.StatusCaixa.ABERTO)) {
            throw CaixaException.caixaJaAberto();
        }

        Usuario operador = getUsuarioLogado();
        Caixa caixa = Caixa.builder()
                .status(Caixa.StatusCaixa.ABERTO)
                .valorAbertura(request.valorAbertura())
                .totalDinheiro(BigDecimal.ZERO)
                .totalPix(BigDecimal.ZERO)
                .totalCartaoDebito(BigDecimal.ZERO)
                .totalCartaoCredito(BigDecimal.ZERO)
                .totalVendas(BigDecimal.ZERO)
                .abertoPor(operador)
                .loja(lojaService.getLojaDoUsuarioLogado())
                .build();

        Caixa salvo = caixaRepository.save(caixa);
        auditService.caixaAberto(salvo.getId(),
                request.valorAbertura().toPlainString());
        log.info("🏪 Caixa #{} ABERTO por {} — Saldo inicial: R$ {}",
                salvo.getId(), operador.getNome(), request.valorAbertura());
        return CaixaResponse.from(salvo);
    }


    // ─── Sangria ─────────────────────────────────────────────────────────
    @Transactional
    public MovimentacaoCaixaResponse registrarMovimentacao(MovimentacaoCaixaRequest request) {
        Caixa caixa = getCaixaAbertoEntity();
        Usuario operador = getUsuarioLogado();

        MovimentacaoCaixa mov = MovimentacaoCaixa.builder()
                .caixa(caixa)
                .tipo(request.tipo())
                .valor(request.valor())
                .motivo(request.motivo())
                .operador(operador)
                .build();

        movimentacaoCaixaRepository.save(mov);

        if (request.tipo() == MovimentacaoCaixa.TipoMovimentacaoCaixa.SANGRIA) {
            caixa.setTotalSangrias(caixa.getTotalSangrias().add(request.valor()));
            caixa.setTotalDinheiro(caixa.getTotalDinheiro().subtract(request.valor()));
        } else {
            caixa.setTotalSuprimentos(caixa.getTotalSuprimentos().add(request.valor()));
            caixa.setTotalDinheiro(caixa.getTotalDinheiro().add(request.valor()));
        }
        caixaRepository.save(caixa);

        log.info("💰 {} de R$ {} no caixa #{} — {}",
                request.tipo(), request.valor(), caixa.getId(), request.motivo());

        return MovimentacaoCaixaResponse.from(mov);
    }

    public List<MovimentacaoCaixaResponse> listarMovimentacoes(Long caixaId) {
        return movimentacaoCaixaRepository.findByCaixaIdOrderByCriadoEmDesc(caixaId)
                .stream().map(MovimentacaoCaixaResponse::from).toList();
    }
    // ─── Fechar Caixa ─────────────────────────────────────────────────────────

    @Transactional
    public ResumoFechamentoCaixaResponse fechar(FecharCaixaRequest request) {
        Caixa caixa = caixaRepository.findByStatus(Caixa.StatusCaixa.ABERTO)
                .orElseThrow(CaixaException::semCaixaAberto);

        Usuario operador = getUsuarioLogado();
        LocalDateTime agora = LocalDateTime.now();

        // Conta vendas finalizadas no período
        List<Venda> vendas = vendaRepository.findFinalizadasNoPeriodo(caixa.getAbertoEm(), agora);
        long qtdVendas = vendas.size();

        // Ticket médio
        BigDecimal ticketMedio = qtdVendas > 0
                ? caixa.getTotalVendas().divide(BigDecimal.valueOf(qtdVendas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Diferença entre o esperado (sistema) e o informado (operador)
        BigDecimal totalEsperado = caixa.getValorAbertura().add(caixa.getTotalDinheiro());
        BigDecimal diferenca = request.valorFechamento().subtract(totalEsperado);

        // Fecha o caixa
        caixa.setStatus(Caixa.StatusCaixa.FECHADO);
        caixa.setValorFechamento(request.valorFechamento());
        caixa.setFechadoPor(operador);
        caixa.setFechadoEm(agora);
        caixa.setObservacaoFechamento(request.observacao());

        Caixa salvo = caixaRepository.save(caixa);
        auditService.caixaFechado(salvo.getId(),
                salvo.getTotalVendas().toPlainString(),
                diferenca.toPlainString());

        log.info("🔒 Caixa #{} FECHADO por {} — Total vendas: R$ {} | Qtd: {} | Diferença: R$ {}",
                salvo.getId(), operador.getNome(), salvo.getTotalVendas(), qtdVendas, diferenca);

        return new ResumoFechamentoCaixaResponse(
                CaixaResponse.from(salvo),
                totalEsperado,
                request.valorFechamento(),
                diferenca,
                qtdVendas,
                ticketMedio
        );
    }

    // ─── Atualização interna de totais (chamada pelo VendaService) ─────────────

    @Transactional
    public void registrarPagamentoNoCaixa(Caixa caixa, Venda.FormaPagamento forma, BigDecimal valor) {
        switch (forma) {
            case DINHEIRO      -> caixa.setTotalDinheiro(caixa.getTotalDinheiro().add(valor));
            case PIX           -> caixa.setTotalPix(caixa.getTotalPix().add(valor));
            case CARTAO_DEBITO -> caixa.setTotalCartaoDebito(caixa.getTotalCartaoDebito().add(valor));
            case CARTAO_CREDITO -> caixa.setTotalCartaoCredito(caixa.getTotalCartaoCredito().add(valor));
            case MISTO         -> caixa.setTotalDinheiro(caixa.getTotalDinheiro().add(valor)); // fallback
        }
        caixa.setTotalVendas(caixa.getTotalVendas().add(valor));
        caixaRepository.save(caixa);
    }

    @Transactional
    public void estornarPagamentoNoCaixa(Caixa caixa, Venda.FormaPagamento forma, BigDecimal valor) {
        switch (forma) {
            case DINHEIRO      -> caixa.setTotalDinheiro(caixa.getTotalDinheiro().subtract(valor));
            case PIX           -> caixa.setTotalPix(caixa.getTotalPix().subtract(valor));
            case CARTAO_DEBITO -> caixa.setTotalCartaoDebito(caixa.getTotalCartaoDebito().subtract(valor));
            case CARTAO_CREDITO -> caixa.setTotalCartaoCredito(caixa.getTotalCartaoCredito().subtract(valor));
            case MISTO         -> caixa.setTotalDinheiro(caixa.getTotalDinheiro().subtract(valor));
        }
        caixa.setTotalVendas(caixa.getTotalVendas().subtract(valor));
        caixaRepository.save(caixa);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    public Caixa getCaixaAbertoEntity() {
        return caixaRepository.findByStatus(Caixa.StatusCaixa.ABERTO)
                .orElseThrow(CaixaException::semCaixaAberto);
    }

    private Caixa findOrThrow(Long id) {
        return caixaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caixa", id));
    }

    private Usuario getUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado"));
    }
}
