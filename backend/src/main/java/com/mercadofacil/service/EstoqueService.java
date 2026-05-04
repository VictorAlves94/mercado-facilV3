package com.mercadofacil.service;

import com.mercadofacil.entity.MovimentacaoEstoque;
import com.mercadofacil.entity.Produto;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.entity.Venda;
import com.mercadofacil.exception.EstoqueInsuficienteException;
import com.mercadofacil.repository.MovimentacaoEstoqueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável por TODAS as movimentações de estoque.
 * Centraliza a lógica para garantir consistência e rastreabilidade.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    /**
     * Reduz estoque por venda. Lança exceção se insuficiente.
     */
    @Transactional
    public void baixarEstoquePorVenda(Produto produto, int quantidade, Venda venda, Usuario operador) {
        validarEstoqueSuficiente(produto, quantidade);
        int anterior = produto.getQuantidadeEstoque();
        produto.setQuantidadeEstoque(anterior - quantidade);
        registrarMovimentacao(produto, MovimentacaoEstoque.TipoMovimentacao.SAIDA_VENDA,
                quantidade, anterior, produto.getQuantidadeEstoque(),
                "Venda #" + venda.getNumeroVenda(), operador, venda);

        if (produto.isEstoqueBaixo()) {
            log.warn("⚠️  ESTOQUE BAIXO: Produto '{}' (ID {}) com apenas {} unidades",
                    produto.getNome(), produto.getId(), produto.getQuantidadeEstoque());
        }
    }

    /**
     * Adiciona estoque (entrada de mercadoria).
     */
    @Transactional
    public void entradaEstoque(Produto produto, int quantidade, String motivo, Usuario operador) {
        int anterior = produto.getQuantidadeEstoque();
        produto.setQuantidadeEstoque(anterior + quantidade);
        registrarMovimentacao(produto, MovimentacaoEstoque.TipoMovimentacao.ENTRADA,
                quantidade, anterior, produto.getQuantidadeEstoque(), motivo, operador, null);
    }

    /**
     * Ajuste manual de inventário.
     */
    @Transactional
    public void ajustarEstoque(Produto produto, int novaQuantidade, String motivo, Usuario operador) {
        int anterior = produto.getQuantidadeEstoque();
        int diferenca = Math.abs(novaQuantidade - anterior);
        MovimentacaoEstoque.TipoMovimentacao tipo = novaQuantidade >= anterior
                ? MovimentacaoEstoque.TipoMovimentacao.AJUSTE_INVENTARIO
                : MovimentacaoEstoque.TipoMovimentacao.SAIDA_AJUSTE;

        produto.setQuantidadeEstoque(novaQuantidade);
        registrarMovimentacao(produto, tipo, diferenca, anterior, novaQuantidade, motivo, operador, null);
    }

    /**
     * Devolve estoque (cancelamento de venda).
     */
    @Transactional
    public void devolverEstoque(Produto produto, int quantidade, Venda venda, Usuario operador) {
        int anterior = produto.getQuantidadeEstoque();
        produto.setQuantidadeEstoque(anterior + quantidade);
        registrarMovimentacao(produto, MovimentacaoEstoque.TipoMovimentacao.DEVOLUCAO,
                quantidade, anterior, produto.getQuantidadeEstoque(),
                "Cancelamento venda #" + venda.getNumeroVenda(), operador, venda);
    }

    private void validarEstoqueSuficiente(Produto produto, int quantidade) {
        if (produto.getQuantidadeEstoque() < quantidade) {
            throw new EstoqueInsuficienteException(
                    produto.getNome(), produto.getQuantidadeEstoque(), quantidade);
        }
    }

    private void registrarMovimentacao(
            Produto produto, MovimentacaoEstoque.TipoMovimentacao tipo,
            int quantidade, int anterior, int posterior,
            String motivo, Usuario usuario, Venda venda) {

        var mov = MovimentacaoEstoque.builder()
                .produto(produto)
                .tipo(tipo)
                .quantidade(quantidade)
                .quantidadeAnterior(anterior)
                .quantidadePosterior(posterior)
                .motivo(motivo)
                .usuario(usuario)
                .venda(venda)
                .build();
        movimentacaoRepository.save(mov);
        log.info("📦 Movimentação [{}] Produto='{}' Qtd={} ({} → {})",
                tipo, produto.getNome(), quantidade, anterior, posterior);
    }
}
