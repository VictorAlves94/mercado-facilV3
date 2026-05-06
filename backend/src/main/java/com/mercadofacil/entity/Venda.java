package com.mercadofacil.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.mercadofacil.entity.Loja;

@Entity
@Table(name = "vendas")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_venda", unique = true, nullable = false)
    private String numeroVenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caixa_id", nullable = false)
    private Caixa caixa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operador_id", nullable = false)
    private Usuario operador;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemVenda> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVenda status;

    @Column(name = "valor_subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSubtotal;

    @Column(name = "valor_desconto", precision = 10, scale = 2)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_recebido", precision = 10, scale = 2)
    private BigDecimal valorRecebido;

    @Column(name = "valor_troco", precision = 10, scale = 2)
    private BigDecimal valorTroco = BigDecimal.ZERO;

    @Column(name = "motivo_cancelamento")
    private String motivoCancelamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id")
    private Loja loja;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        if (valorDesconto == null) valorDesconto = BigDecimal.ZERO;
        if (valorTroco == null) valorTroco = BigDecimal.ZERO;
    }

    public void addItem(ItemVenda item) {
        itens.add(item);
        item.setVenda(this);
    }

    public void calcularTotais() {
        this.valorSubtotal = itens.stream()
                .filter(i -> i.getStatus() == ItemVenda.StatusItem.ATIVO)
                .map(ItemVenda::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.valorTotal = valorSubtotal.subtract(
                valorDesconto != null ? valorDesconto : BigDecimal.ZERO);
    }

    public enum FormaPagamento {
        DINHEIRO, PIX, CARTAO_DEBITO, CARTAO_CREDITO, MISTO
    }

    public enum StatusVenda {
        ABERTA, FINALIZADA, CANCELADA
    }
}
