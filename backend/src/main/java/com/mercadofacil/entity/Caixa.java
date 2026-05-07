package com.mercadofacil.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.mercadofacil.entity.Loja;

@Entity
@Table(name = "caixas")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCaixa status;

    @Column(name = "valor_abertura", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorAbertura;

    @Column(name = "valor_fechamento", precision = 10, scale = 2)
    private BigDecimal valorFechamento;

    @Column(name = "total_dinheiro", precision = 10, scale = 2)
    private BigDecimal totalDinheiro = BigDecimal.ZERO;

    @Column(name = "total_pix", precision = 10, scale = 2)
    private BigDecimal totalPix = BigDecimal.ZERO;

    @Column(name = "total_cartao_debito", precision = 10, scale = 2)
    private BigDecimal totalCartaoDebito = BigDecimal.ZERO;

    @Column(name = "total_cartao_credito", precision = 10, scale = 2)
    private BigDecimal totalCartaoCredito = BigDecimal.ZERO;

    @Column(name = "total_vendas", precision = 10, scale = 2)
    private BigDecimal totalVendas = BigDecimal.ZERO;

    @Column(name = "total_sangrias", precision = 10, scale = 2)
    private BigDecimal totalSangrias = BigDecimal.ZERO;

    @Column(name = "total_suprimentos", precision = 10, scale = 2)
    private BigDecimal totalSuprimentos = BigDecimal.ZERO;

    @Column(name = "observacao_fechamento")
    private String observacaoFechamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aberto_por", nullable = false)
    private Usuario abertoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fechado_por")
    private Usuario fechadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id")
    private Loja loja;

    @Column(name = "aberto_em", nullable = false)
    private LocalDateTime abertoEm;

    @Column(name = "fechado_em")
    private LocalDateTime fechadoEm;

    @OneToMany(mappedBy = "caixa")
    private List<Venda> vendas;

    @PrePersist
    protected void onCreate() {
        abertoEm = LocalDateTime.now();
        if (totalDinheiro == null) totalDinheiro = BigDecimal.ZERO;
        if (totalPix == null) totalPix = BigDecimal.ZERO;
        if (totalCartaoDebito == null) totalCartaoDebito = BigDecimal.ZERO;
        if (totalCartaoCredito == null) totalCartaoCredito = BigDecimal.ZERO;
        if (totalVendas == null) totalVendas = BigDecimal.ZERO;
        if (totalSangrias == null)    totalSangrias    = BigDecimal.ZERO;
        if (totalSuprimentos == null) totalSuprimentos = BigDecimal.ZERO;
    }

    public BigDecimal getTotalGeral() {
        return totalDinheiro
                .add(totalPix)
                .add(totalCartaoDebito)
                .add(totalCartaoCredito);
    }

    public enum StatusCaixa {
        ABERTO, FECHADO
    }
}
