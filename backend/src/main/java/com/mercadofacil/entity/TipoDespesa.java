package com.mercadofacil.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_despesa")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TipoDespesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String descricao;

    private boolean ativo = true;
}
