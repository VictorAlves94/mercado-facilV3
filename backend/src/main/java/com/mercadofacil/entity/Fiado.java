package com.mercadofacil.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controle de Fiado — Caderninho digital.
 * Diferencial comercial: muito comum em mercados de bairro.
 */
@Entity
@Table(name = "fiados")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Fiado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_cliente", nullable = false)
    private String nomeCliente;

    @Column(name = "telefone_cliente")
    private String telefoneCliente;

    @Column(name = "saldo_devedor", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoDevedor = BigDecimal.ZERO;

    @Column(name = "limite_credito", precision = 10, scale = 2)
    private BigDecimal limiteCredito;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFiado status = StatusFiado.ATIVO;

    @Column(name = "data_ultimo_lancamento")
    private LocalDate dataUltimoLancamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por", nullable = false)
    private Usuario registradoPor;

    @OneToMany(mappedBy = "fiado", cascade = CascadeType.ALL)
    private List<LancamentoFiado> lancamentos;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    public boolean isLimitoExcedido(BigDecimal valorNovo) {
        if (limiteCredito == null) return false;
        return saldoDevedor.add(valorNovo).compareTo(limiteCredito) > 0;
    }

    public enum StatusFiado {
        ATIVO, QUITADO, BLOQUEADO
    }
}
