package com.mercadofacil.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_usuario",   columnList = "usuario_id"),
        @Index(name = "idx_audit_entidade",  columnList = "entidade, entidade_id"),
        @Index(name = "idx_audit_criado_em", columnList = "criado_em")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "usuario_nome", nullable = false)
    private String usuarioNome; // snapshot do nome

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAcao acao;

    @Column(nullable = false)
    private String descricao; // "João cancelou a venda #V00012"

    @Column(nullable = false)
    private String entidade; // "Venda", "Produto", "Despesa"

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(name = "entidade_referencia")
    private String entidadeReferencia; // "V20250601001", "Arroz 5kg"

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_posterior", columnDefinition = "TEXT")
    private String valorPosterior;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    public enum TipoAcao {
        VENDA_CRIADA, VENDA_CANCELADA,
        PRODUTO_CRIADO, PRODUTO_EDITADO, PRODUTO_DESATIVADO,
        ESTOQUE_ENTRADA, ESTOQUE_AJUSTE,
        CAIXA_ABERTO, CAIXA_FECHADO,
        DESPESA_LANCADA, DESPESA_EDITADA, DESPESA_EXCLUIDA,
        FIADO_DEBITO, FIADO_PAGAMENTO, FIADO_QUITADO, FIADO_BLOQUEADO,
        LOGIN_REALIZADO, LOGIN_FALHOU
    }

}
