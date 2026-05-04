package com.mercadofacil.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lancamentos_fiado")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class LancamentoFiado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiado_id", nullable = false)
    private Fiado fiado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLancamento tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por", nullable = false)
    private Usuario registradoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    public enum TipoLancamento {
        DEBITO, PAGAMENTO
    }
}
