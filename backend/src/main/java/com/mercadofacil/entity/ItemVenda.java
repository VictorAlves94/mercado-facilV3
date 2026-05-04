package com.mercadofacil.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "itens_venda")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "preco_custo_snapshot", precision = 10, scale = 2)
    private BigDecimal precoCustoSnapshot;

    @Column(precision = 5, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusItem status = StatusItem.ATIVO;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        calcularSubtotal();
    }

    public void calcularSubtotal() {
        BigDecimal desc = desconto != null ? desconto : BigDecimal.ZERO;
        BigDecimal precoComDesconto = precoUnitario.multiply(BigDecimal.ONE.subtract(desc.divide(BigDecimal.valueOf(100))));
        this.subtotal = precoComDesconto.multiply(BigDecimal.valueOf(quantidade));
    }

    public BigDecimal getLucroItem() {
        if (precoCustoSnapshot == null) return BigDecimal.ZERO;
        return subtotal.subtract(precoCustoSnapshot.multiply(BigDecimal.valueOf(quantidade)));
    }

    public enum StatusItem {
        ATIVO, CANCELADO
    }
}
