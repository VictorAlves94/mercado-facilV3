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

    // ← ADICIONAR — filtrados por loja
    @Query("SELECT c FROM Caixa c WHERE c.status = :status " +
            "AND (:lojaId IS NULL OR c.loja.id = :lojaId)")
    Optional<Caixa> findByStatusAndLojaId(
            @Param("status") Caixa.StatusCaixa status,
            @Param("lojaId") Long lojaId);

    @Query("SELECT COUNT(c) > 0 FROM Caixa c WHERE c.status = :status " +
            "AND (:lojaId IS NULL OR c.loja.id = :lojaId)")
    boolean existsByStatusAndLojaId(
            @Param("status") Caixa.StatusCaixa status,
            @Param("lojaId") Long lojaId);

    @Query("SELECT c FROM Caixa c WHERE c.abertoEm >= :inicio AND c.abertoEm < :fim " +
            "ORDER BY c.abertoEm DESC")
    List<Caixa> findByPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim);

    @Query("SELECT c FROM Caixa c ORDER BY c.abertoEm DESC")
    List<Caixa> findAllOrderByAbertoemDesc();

    // ← ADICIONAR — histórico filtrado por loja
    @Query("SELECT c FROM Caixa c WHERE (:lojaId IS NULL OR c.loja.id = :lojaId) " +
            "ORDER BY c.abertoEm DESC")
    List<Caixa> findAllByLojaOrderByAbertoemDesc(@Param("lojaId") Long lojaId);
}
