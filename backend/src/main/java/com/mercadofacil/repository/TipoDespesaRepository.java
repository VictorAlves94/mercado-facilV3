package com.mercadofacil.repository;

import com.mercadofacil.entity.TipoDespesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TipoDespesaRepository extends JpaRepository<TipoDespesa, Long> {
    List<TipoDespesa> findByAtivoTrueOrderByNomeAsc();
}
