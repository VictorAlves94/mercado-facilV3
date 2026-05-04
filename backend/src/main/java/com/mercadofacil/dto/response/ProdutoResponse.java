package com.mercadofacil.dto.response;

import com.mercadofacil.entity.Produto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProdutoResponse(
    Long id,
    String codigoBarras,
    String nome,
    String descricao,
    Long categoriaId,
    String categoriaNome,
    Integer quantidadeEstoque,
    Integer estoqueMinimo,
    BigDecimal precoCusto,
    BigDecimal precoVenda,
    BigDecimal margem,
    LocalDate dataValidade,
    boolean ativo,
    boolean estoqueBaixo,
    boolean estoqueZerado,
    boolean validadeProxima,
    boolean vencido,
    LocalDateTime criadoEm,
    LocalDateTime atualizadoEm
) {
    public static ProdutoResponse from(Produto p) {
        return new ProdutoResponse(
            p.getId(),
            p.getCodigoBarras(),
            p.getNome(),
            p.getDescricao(),
            p.getCategoria() != null ? p.getCategoria().getId() : null,
            p.getCategoria() != null ? p.getCategoria().getNome() : null,
            p.getQuantidadeEstoque(),
            p.getEstoqueMinimo(),
            p.getPrecoCusto(),
            p.getPrecoVenda(),
            p.calcularMargem(),
            p.getDataValidade(),
            p.isAtivo(),
            p.isEstoqueBaixo(),
            p.isEstoqueZerado(),
            p.isValidadeProxima(7),
            p.isVencido(),
            p.getCriadoEm(),
            p.getAtualizadoEm()
        );
    }
}
