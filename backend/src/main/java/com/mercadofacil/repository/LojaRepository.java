package com.mercadofacil.repository;

import com.mercadofacil.entity.Loja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LojaRepository extends JpaRepository<Loja, Long> {
    List<Loja> findByAtivaOrderByNomeAsc(boolean ativa);
    Optional<Loja> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}

