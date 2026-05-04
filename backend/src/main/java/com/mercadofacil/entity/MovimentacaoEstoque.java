package com.mercadofacil.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "quantidade_anterior", nullable = false)
    private Integer quantidadeAnterior;

    @Column(name = "quantidade_posterior", nullable = false)
    private Integer quantidadePosterior;

    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    public enum TipoMovimentacao {
        ENTRADA, SAIDA_VENDA, SAIDA_AJUSTE, AJUSTE_INVENTARIO, DEVOLUCAO
    }
}
