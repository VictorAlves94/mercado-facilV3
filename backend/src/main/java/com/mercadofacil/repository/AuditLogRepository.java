package com.mercadofacil.repository;

import com.mercadofacil.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Feed principal com filtros
    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:usuarioId IS NULL OR a.usuario.id = :usuarioId)
        AND (:entidade IS NULL OR a.entidade = :entidade)
        AND (a.criadoEm >= :inicio AND a.criadoEm < :fim)
        ORDER BY a.criadoEm DESC
        """)
    Page<AuditLog> buscarComFiltros(
            @Param("usuarioId") Long usuarioId,
            @Param("entidade")  String entidade,
            @Param("inicio")    LocalDateTime inicio,
            @Param("fim")       LocalDateTime fim,
            Pageable pageable);

    // Histórico de uma entidade (ex: tudo que aconteceu com a Venda #12)
    List<AuditLog> findByEntidadeAndEntidadeIdOrderByCriadoEmDesc(
            String entidade, Long entidadeId);

    // Ações suspeitas do dia: cancelamentos, exclusões, ajustes manuais
    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.acao IN ('VENDA_CANCELADA', 'DESPESA_EXCLUIDA',
                         'ESTOQUE_AJUSTE', 'PRODUTO_DESATIVADO')
        AND a.criadoEm >= :inicio AND a.criadoEm < :fim
        ORDER BY a.criadoEm DESC
        """)
    List<AuditLog> findAcoesSuspeitasNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim);

    // Quem cancelou mais vendas hoje (detectar abuso)
    @Query("""
        SELECT a.usuarioNome, COUNT(a)
        FROM AuditLog a
        WHERE a.acao = 'VENDA_CANCELADA'
        AND a.criadoEm >= :inicio AND a.criadoEm < :fim
        GROUP BY a.usuarioNome
        ORDER BY COUNT(a) DESC
        """)
    List<Object[]> countCancelamentosPorUsuario(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim")    LocalDateTime fim);

}
