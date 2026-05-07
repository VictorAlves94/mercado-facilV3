package com.mercadofacil.repository;

import com.mercadofacil.entity.MovimentacaoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MovimentacaoCaixaRepository extends JpaRepository<MovimentacaoCaixa, Long>{

    List<MovimentacaoCaixa> findByCaixaIdOrderByCriadoEmDesc(Long caixaId);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentacaoCaixa m WHERE m.caixa.id = :caixaId AND m.tipo = 'SANGRIA'")
    BigDecimal sumSangriasByCaixa(@Param("caixaId") Long caixaId);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimentacaoCaixa m WHERE m.caixa.id = :caixaId AND m.tipo = 'SUPRIMENTO'")
    BigDecimal sumSuprimentosByCaixa(@Param("caixaId") Long caixaId);

}
