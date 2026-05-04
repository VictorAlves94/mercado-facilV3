package com.mercadofacil.repository;

import com.mercadofacil.entity.LancamentoFiado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LancamentoFiadoRepository extends JpaRepository<LancamentoFiado, Long> {

    List<LancamentoFiado> findByFiadoIdOrderByCriadoEmDesc(Long fiadoId);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM LancamentoFiado l WHERE l.fiado.id = :fiadoId AND l.tipo = 'DEBITO'")
    BigDecimal sumDebitosByFiado(@Param("fiadoId") Long fiadoId);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM LancamentoFiado l WHERE l.fiado.id = :fiadoId AND l.tipo = 'PAGAMENTO'")
    BigDecimal sumPagamentosByFiado(@Param("fiadoId") Long fiadoId);
}
