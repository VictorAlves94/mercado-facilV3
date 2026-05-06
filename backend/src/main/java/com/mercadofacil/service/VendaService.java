package com.mercadofacil.service;

import com.mercadofacil.dto.request.CancelarVendaRequest;
import com.mercadofacil.dto.request.VendaRequest;
import com.mercadofacil.dto.response.PageResponse;
import com.mercadofacil.dto.response.VendaResponse;
import com.mercadofacil.entity.*;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.CaixaException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CaixaService caixaService;
    private final EstoqueService estoqueService;
    private final AuditService auditService;

    // contador thread-safe para número de venda no dia
    private static final AtomicLong contadorVenda = new AtomicLong(0);

    // ─── Consultas ────────────────────────────────────────────────────────────

    public VendaResponse buscarPorId(Long id) {
        return VendaResponse.from(findComItensOrThrow(id));
    }

    public VendaResponse buscarPorNumero(String numero) {
        Venda venda = vendaRepository.findByNumeroVenda(numero)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada: " + numero));
        return VendaResponse.from(venda);
    }

    public PageResponse<VendaResponse> listarPorCaixa(Long caixaId, int pagina, int tamanho) {
        var pageable = PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending());
        return PageResponse.from(
                vendaRepository.findByCaixaIdOrderByCriadoEmDesc(caixaId, pageable)
                        .map(VendaResponse::from));
    }

    public List<VendaResponse> listarHoje() {
        LocalDateTime inicio = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fim = inicio.plusDays(1);
        return vendaRepository.findFinalizadasNoPeriodo(inicio, fim)
                .stream().map(VendaResponse::from).toList();
    }

    // ─── Registrar Venda ──────────────────────────────────────────────────────

    @Transactional
    public VendaResponse registrar(VendaRequest request) {
        // 1. Valida caixa aberto
        Caixa caixa = caixaService.getCaixaAbertoEntity();
        Usuario operador = getUsuarioLogado();

        // 2. Monta a venda
        Venda venda = Venda.builder()
                .numeroVenda(gerarNumeroVenda())
                .caixa(caixa)
                .operador(operador)
                .formaPagamento(request.formaPagamento())
                .status(Venda.StatusVenda.ABERTA)
                .valorDesconto(request.descontoGeral())
                .valorSubtotal(BigDecimal.ZERO)
                .valorTotal(BigDecimal.ZERO)
                .build();

        // 3. Processa cada item
        for (var itemReq : request.itens()) {
            Produto produto = produtoRepository.findById(itemReq.produtoId())
                    .filter(Produto::isAtivo)
                    .orElseThrow(() -> new ResourceNotFoundException("Produto", itemReq.produtoId()));

            // Valida e baixa estoque atomicamente
            estoqueService.baixarEstoquePorVenda(produto, itemReq.quantidade(), venda, operador);
            produtoRepository.save(produto);

            ItemVenda item = ItemVenda.builder()
                    .produto(produto)
                    .quantidade(itemReq.quantidade())
                    .precoUnitario(produto.getPrecoVenda())
                    .precoCustoSnapshot(produto.getPrecoCusto())
                    .desconto(itemReq.desconto())
                    .status(ItemVenda.StatusItem.ATIVO)
                    .build();
            item.calcularSubtotal();
            venda.addItem(item);
        }

        // 4. Calcula totais
        venda.calcularTotais();

        // 5. Valida pagamento
        validarPagamento(request, venda.getValorTotal());

        // 6. Calcula troco (apenas dinheiro)
        if (request.formaPagamento() == Venda.FormaPagamento.DINHEIRO && request.valorRecebido() != null) {
            BigDecimal troco = request.valorRecebido().subtract(venda.getValorTotal());
            venda.setValorRecebido(request.valorRecebido());
            venda.setValorTroco(troco.max(BigDecimal.ZERO));
        } else if (request.valorRecebido() != null) {
            venda.setValorRecebido(request.valorRecebido());
            venda.setValorTroco(BigDecimal.ZERO);
        }

        // 7. Finaliza
        venda.setStatus(Venda.StatusVenda.FINALIZADA);
        Venda salva = vendaRepository.save(venda);
        auditService.vendaCriada(salva.getId(), salva.getNumeroVenda(),
                salva.getValorTotal().toPlainString());


        // 8. Atualiza totais no caixa
        caixaService.registrarPagamentoNoCaixa(caixa, request.formaPagamento(), salva.getValorTotal());

        log.info("✅ Venda {} finalizada — R$ {} | {} | Operador: {}",
                salva.getNumeroVenda(), salva.getValorTotal(),
                salva.getFormaPagamento(), operador.getNome());

        return VendaResponse.from(salva);
    }

    // ─── Cancelar Venda ──────────────────────────────────────────────────────

    @Transactional
    public VendaResponse cancelar(Long id, CancelarVendaRequest request) {
        Venda venda = findComItensOrThrow(id);
        Usuario operador = getUsuarioLogado();

        // Validações
        if (venda.getStatus() == Venda.StatusVenda.CANCELADA) {
            throw new BusinessException("Venda #" + venda.getNumeroVenda() + " já está cancelada.");
        }
        if (venda.getStatus() != Venda.StatusVenda.FINALIZADA) {
            throw new BusinessException("Somente vendas finalizadas podem ser canceladas.");
        }

        // Devolve estoque de cada item ativo
        for (ItemVenda item : venda.getItens()) {
            if (item.getStatus() == ItemVenda.StatusItem.ATIVO) {
                Produto produto = item.getProduto();
                estoqueService.devolverEstoque(produto, item.getQuantidade(), venda, operador);
                produtoRepository.save(produto);
                item.setStatus(ItemVenda.StatusItem.CANCELADO);
            }
        }

        // Estorna do caixa
        if (venda.getCaixa() != null && venda.getCaixa().getStatus() == Caixa.StatusCaixa.ABERTO) {
            caixaService.estornarPagamentoNoCaixa(
                    venda.getCaixa(), venda.getFormaPagamento(), venda.getValorTotal());
        }

        // Cancela a venda
        venda.setStatus(Venda.StatusVenda.CANCELADA);
        venda.setMotivoCancelamento(request.motivo());
        venda.setCanceladoEm(LocalDateTime.now());

        Venda salva = vendaRepository.save(venda);
        auditService.vendaCancelada(salva.getId(), salva.getNumeroVenda(),
                request.motivo());


        log.info("❌ Venda {} CANCELADA por {} — Motivo: {}",
                salva.getNumeroVenda(), operador.getNome(), request.motivo());

        return VendaResponse.from(salva);
    }

    // ─── Helpers privados ─────────────────────────────────────────────────────

    private void validarPagamento(VendaRequest request, BigDecimal valorTotal) {
        if (request.formaPagamento() == Venda.FormaPagamento.DINHEIRO) {
            if (request.valorRecebido() == null) {
                throw new BusinessException("Informe o valor recebido para pagamento em dinheiro.");
            }
            if (request.valorRecebido().compareTo(valorTotal) < 0) {
                throw new BusinessException(
                        String.format("Valor recebido (R$ %.2f) insuficiente. Total: R$ %.2f",
                                request.valorRecebido(), valorTotal));
            }
        }
    }

    private String gerarNumeroVenda() {
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = contadorVenda.incrementAndGet();
        return String.format("V%s%04d", data, seq);
    }

    private Venda findComItensOrThrow(Long id) {
        return vendaRepository.findByIdWithItens(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
    }

    private Usuario getUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário logado não encontrado"));
    }
}
