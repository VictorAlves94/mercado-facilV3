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
        LEFT JOIN FETCH p.categoria
        WHERE p.ativo = true
        AND (:busca IS NULL OR
             LOWER(p.nome) LIKE LOWER(CONCAT('%', :busca, '%')) OR
             p.codigoBarras LIKE CONCAT('%', :busca, '%'))
        AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
        """)
    Page<Produto> buscarProdutos(
            @Param("busca") String busca,
            @Param("categoriaId") Long categoriaId,
            Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque <= p.estoqueMinimo")
    List<Produto> findEstoqueBaixo();

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque = 0")
    List<Produto> findEstoqueZerado();

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.dataValidade IS NOT NULL AND p.dataValidade <= :dataLimite")
    List<Produto> findValidadeProxima(@Param("dataLimite") LocalDate dataLimite);

    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND p.dataValidade IS NOT NULL AND p.dataValidade < :hoje")
    List<Produto> findVencidos(@Param("hoje") LocalDate hoje);

    @Query("SELECT COUNT(p) FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque <= p.estoqueMinimo")
    long countEstoqueBaixo();

    @Query("SELECT COUNT(p) FROM Produto p WHERE p.ativo = true AND p.quantidadeEstoque = 0")
    long countEstoqueZerado();
}
