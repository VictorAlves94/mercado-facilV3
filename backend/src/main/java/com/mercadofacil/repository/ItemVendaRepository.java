package com.mercadofacil.repository;

import com.mercadofacil.entity.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {

    List<ItemVenda> findByVendaId(Long vendaId);

    @Query("""
        SELECT i FROM ItemVenda i
        JOIN FETCH i.produto
        WHERE i.venda.id = :vendaId
        AND i.status = 'ATIVO'
        """)
    List<ItemVenda> findAtivosporVenda(@Param("vendaId") Long vendaId);
}
