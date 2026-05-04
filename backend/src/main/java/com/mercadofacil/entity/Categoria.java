package com.mercadofacil.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categorias")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String descricao;

    @OneToMany(mappedBy = "categoria")
    private List<Produto> produtos;

    @Column(nullable = false)
    private boolean ativo = true;
}
