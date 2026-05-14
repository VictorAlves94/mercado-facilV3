package com.mercadofacil.repository;

import com.mercadofacil.entity.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    List<Despesa> findByDataDespesaBetweenOrderByDataDespesaDesc(LocalDate inicio, LocalDate fim);

    List<Despesa> findByDataDespesaOrderByCriadoEmDesc(LocalDate data);

    @Query("SELECT COALESCE(SUM(d.valor), 0) FROM Despesa d WHERE d.dataDespesa >= :inicio AND d.dataDespesa <= :fim")
    BigDecimal sumTotalNoPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
        SELECT COALESCE(SUM(d.valor), 0)
        FROM Despesa d
        WHERE d.dataDespesa >= :inicio
          AND d.dataDespesa <= :fim
          AND d.loja.id = :lojaId
    """)
    BigDecimal sumTotalNoPeriodoPorLoja(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("lojaId") Long lojaId
    );

    @Query("SELECT d.tipoDespesa.nome, SUM(d.valor), COUNT(d) FROM Despesa d WHERE d.dataDespesa >= :inicio AND d.dataDespesa <= :fim GROUP BY d.tipoDespesa.nome ORDER BY SUM(d.valor) DESC")
    List<Object[]> findTotalAgrupadoPorTipo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
        SELECT d.tipoDespesa.nome, COALESCE(SUM(d.valor), 0), COUNT(d)
        FROM Despesa d
        WHERE d.dataDespesa >= :inicio
          AND d.dataDespesa <= :fim
          AND d.loja.id = :lojaId
        GROUP BY d.tipoDespesa.nome
        ORDER BY SUM(d.valor) DESC
    """)
    List<Object[]> findTotalAgrupadoPorTipoPorLoja(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("lojaId") Long lojaId
    );

    @Query("SELECT d.dataDespesa, COALESCE(SUM(d.valor), 0) FROM Despesa d WHERE d.dataDespesa >= :inicio AND d.dataDespesa <= :fim GROUP BY d.dataDespesa ORDER BY d.dataDespesa ASC")
    List<Object[]> findTotalPorDia(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
        SELECT d.dataDespesa, COALESCE(SUM(d.valor), 0)
        FROM Despesa d
        WHERE d.dataDespesa >= :inicio
          AND d.dataDespesa <= :fim
          AND d.loja.id = :lojaId
        GROUP BY d.dataDespesa
        ORDER BY d.dataDespesa ASC
    """)
    List<Object[]> findTotalPorDiaPorLoja(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("lojaId") Long lojaId
    );
}