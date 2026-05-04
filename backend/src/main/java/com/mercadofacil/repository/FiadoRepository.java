package com.mercadofacil.repository;

import com.mercadofacil.entity.Fiado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FiadoRepository extends JpaRepository<Fiado, Long> {

    List<Fiado> findByStatusOrderByNomeClienteAsc(Fiado.StatusFiado status);

    @Query("SELECT f FROM Fiado f WHERE LOWER(f.nomeCliente) LIKE LOWER(CONCAT('%', :nome, '%')) ORDER BY f.nomeCliente ASC")
    List<Fiado> findByNomeClienteContainingIgnoreCase(@Param("nome") String nome);

    Optional<Fiado> findByNomeClienteIgnoreCaseAndStatus(String nomeCliente, Fiado.StatusFiado status);

    // Total em aberto para relatório financeiro
    @Query("SELECT COALESCE(SUM(f.saldoDevedor), 0) FROM Fiado f WHERE f.status = 'ATIVO'")
    BigDecimal sumSaldoDevedorAtivo();

    @Query("SELECT COUNT(f) FROM Fiado f WHERE f.status = 'ATIVO' AND f.saldoDevedor > 0")
    long countClientesComSaldoAtivo();
}
