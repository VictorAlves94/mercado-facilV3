package com.mercadofacil.repository;

import com.mercadofacil.entity.Caixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaixaRepository extends JpaRepository<Caixa, Long> {

    Optional<Caixa> findByStatus(Caixa.StatusCaixa status);

    boolean existsByStatus(Caixa.StatusCaixa status);

    @Query("SELECT c FROM Caixa c WHERE c.abertoEm >= :inicio AND c.abertoEm < :fim ORDER BY c.abertoEm DESC")
    List<Caixa> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT c FROM Caixa c ORDER BY c.abertoEm DESC")
    List<Caixa> findAllOrderByAbertoemDesc();
}
