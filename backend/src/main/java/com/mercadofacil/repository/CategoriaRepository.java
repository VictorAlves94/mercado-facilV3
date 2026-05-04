package com.mercadofacil.repository;

import com.mercadofacil.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByOrderByNomeAsc();
    boolean existsByNome(String nome);
}
