package com.mercadofacil.repository;

import com.mercadofacil.entity.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    Optional<Venda> findByNumeroVenda(String numeroVenda);

    Page<Venda> findByCaixaIdOrderByCriadoEmDesc(Long caixaId, Pageable pageable);

    @Query("""
        SELECT v FROM Venda v
        WHERE v.criadoEm >= :inicio
          AND v.criadoEm < :fim
          AND v.status = 'FINALIZADA'
        ORDER BY v.criadoEm DESC
    """)
    List<Venda> findFinalizadasNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim);

// ─── Dashboard — chamado pelo DashboardService ────────────────────────────

    @Query("""
    SELECT COALESCE(SUM(v.valorTotal), 0)
    FROM Venda v
    WHERE v.criadoEm >= :inicio
      AND v.criadoEm < :fim
      AND v.status = 'FINALIZADA'
      AND v.loja.id = :lojaId
""")
    BigDecimal sumTotalNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim,
            @Param("lojaId") Long lojaId);

    @Query("""
    SELECT COUNT(v)
    FROM Venda v
    WHERE v.criadoEm >= :inicio
      AND v.criadoEm < :fim
      AND v.status = 'FINALIZADA'
      AND v.loja.id = :lojaId
""")
    long countFinalizadasNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim,
            @Param("lojaId") Long lojaId);

// ─── Financeiro/Relatório — chamado pelo FinanceiroService ───────────────
// Mesmo SQL, nome diferente para não conflitar com a assinatura acima

    @Query("""
    SELECT COALESCE(SUM(v.valorTotal), 0)
    FROM Venda v
    WHERE v.criadoEm >= :inicio
      AND v.criadoEm < :fim
      AND v.status = 'FINALIZADA'
      AND v.loja.id = :lojaId
""")
    BigDecimal sumTotalNoPeriodoPorLoja(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim,
            @Param("lojaId") Long lojaId);

    @Query("""
    SELECT COUNT(v)
    FROM Venda v
    WHERE v.criadoEm >= :inicio
      AND v.criadoEm < :fim
      AND v.status = 'FINALIZADA'
      AND v.loja.id = :lojaId
""")
    long countFinalizadasNoPeriodoPorLoja(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim,
            @Param("lojaId") Long lojaId);


    // ─── Busca por id com itens ──────────────────────────────────────────────

    @Query("""
        SELECT v FROM Venda v
        LEFT JOIN FETCH v.itens i
        LEFT JOIN FETCH i.produto
        WHERE v.id = :id
    """)
    Optional<Venda> findByIdWithItens(@Param("id") Long id);

    // ─── Relatórios ─────────────────────────────────────────────────────────

    @Query("""
        SELECT COALESCE(SUM(v.valorTotal), 0)
        FROM Venda v
        WHERE v.criadoEm >= :inicio
          AND v.criadoEm < :fim
          AND v.status = 'FINALIZADA'
          AND v.formaPagamento = :forma
    """)
    BigDecimal sumTotalPorFormaPagamento(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim,
            @Param("forma")  Venda.FormaPagamento forma);

    @Query("""
        SELECT COALESCE(AVG(v.valorTotal), 0)
        FROM Venda v
        WHERE v.criadoEm >= :inicio
          AND v.criadoEm < :fim
          AND v.status = 'FINALIZADA'
    """)
    BigDecimal avgTicketNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim);

    @Query("""
        SELECT i.produto.id, i.produto.nome, SUM(i.quantidade) as total
        FROM ItemVenda i
        WHERE i.venda.criadoEm >= :inicio
          AND i.venda.criadoEm < :fim
          AND i.venda.status = 'FINALIZADA'
          AND i.status = 'ATIVO'
        GROUP BY i.produto.id, i.produto.nome
        ORDER BY total DESC
    """)
    List<Object[]> findProdutosMaisVendidos(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim,
            Pageable pageable);

    @Query("""
        SELECT COUNT(v)
        FROM Venda v
        WHERE v.criadoEm >= :inicio
          AND v.criadoEm < :fim
          AND v.status = 'CANCELADA'
    """)
    long countCanceladasNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim);

    @Query("""
        SELECT COALESCE(SUM(v.valorTotal), 0)
        FROM Venda v
        WHERE v.criadoEm >= :inicio
          AND v.criadoEm < :fim
          AND v.status = 'CANCELADA'
    """)
    BigDecimal sumCanceladasNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim);
}
