package com.mercadofacil.repository;

import com.mercadofacil.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

 Optional<Produto> findByCodigoBarras(String codigoBarras);

 @Query("""
    SELECT p FROM Produto p
    WHERE p.codigoBarras = :codigo
    AND p.ativo = true
    AND (:lojaId IS NULL OR p.loja.id = :lojaId)
    """)
Optional<Produto> findByCodigoBarrasAndLojaId(
        @Param("codigo") String codigoBarras,
        @Param("lojaId") Long lojaId);

@Query("""
    SELECT p FROM Produto p
    LEFT JOIN p.categoria
    WHERE p.ativo = true
    AND (:lojaId IS NULL OR p.loja.id = :lojaId)
    AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
    """)
Page<Produto> listarProdutos(
        @Param("lojaId") Long lojaId,
        @Param("categoriaId") Long categoriaId,
        Pageable pageable);

@Query("""
    SELECT p FROM Produto p
    LEFT JOIN p.categoria
    WHERE p.ativo = true
    AND (:lojaId IS NULL OR p.loja.id = :lojaId)
    AND (
        LOWER(p.nome) LIKE CONCAT('%', LOWER(:busca), '%')
        OR p.codigoBarras LIKE CONCAT('%', :busca, '%')
    )
    AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
    """)
Page<Produto> buscarProdutos(
        @Param("lojaId") Long lojaId,
        @Param("busca") String busca,
        @Param("categoriaId") Long categoriaId,
        Pageable pageable);


    // ─── Alertas com filtro de loja (null = todas as lojas) ──────────────────
    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque <= p.estoqueMinimo AND (:lojaId IS NULL OR p.loja.id = :lojaId)")
    List<Produto> findEstoqueBaixo(@Param("lojaId") Long lojaId);

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque = 0 AND (:lojaId IS NULL OR p.loja.id = :lojaId)")
    List<Produto> findEstoqueZerado(@Param("lojaId") Long lojaId);

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.dataValidade IS NOT NULL AND p.dataValidade <= :dataLimite AND (:lojaId IS NULL OR p.loja.id = :lojaId)")
    List<Produto> findValidadeProxima(
            @Param("dataLimite") LocalDate dataLimite,
            @Param("lojaId")     Long lojaId);

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.dataValidade IS NOT NULL AND p.dataValidade < :hoje AND (:lojaId IS NULL OR p.loja.id = :lojaId)")
    List<Produto> findVencidos(
            @Param("hoje")   LocalDate hoje,
            @Param("lojaId") Long lojaId);

    @Query("SELECT COUNT(p) FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque <= p.estoqueMinimo AND (:lojaId IS NULL OR p.loja.id = :lojaId)")
    long countEstoqueBaixo(@Param("lojaId") Long lojaId);

    @Query("SELECT COUNT(p) FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque = 0 AND (:lojaId IS NULL OR p.loja.id = :lojaId)")
    long countEstoqueZerado(@Param("lojaId") Long lojaId);
}
