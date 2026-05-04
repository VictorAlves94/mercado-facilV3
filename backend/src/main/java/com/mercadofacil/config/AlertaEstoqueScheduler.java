package com.mercadofacil.config;

import com.mercadofacil.dto.response.AlertasEstoqueResponse;
import com.mercadofacil.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler de alertas de estoque.
 * Roda a cada hora em produção e loga no console.
 * Base para futuras integrações: WhatsApp, e-mail, push notification.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertaEstoqueScheduler {

    private final ProdutoService produtoService;

    /** Roda toda hora */
    @Scheduled(cron = "0 0 * * * *")
    public void verificarAlertas() {
        AlertasEstoqueResponse alertas = produtoService.getAlertas();

        if (alertas.totalAlertas() == 0) return;

        log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.warn("🚨 ALERTAS DE ESTOQUE — {} ocorrência(s)", alertas.totalAlertas());

        if (!alertas.estoqueZerado().isEmpty()) {
            log.warn("❌ ZERADOS ({}):", alertas.estoqueZerado().size());
            alertas.estoqueZerado().forEach(p ->
                log.warn("   • {} — 0 unidades", p.nome()));
        }

        if (!alertas.estoqueBaixo().isEmpty()) {
            log.warn("⚠️  ESTOQUE BAIXO ({}):", alertas.estoqueBaixo().size());
            alertas.estoqueBaixo().forEach(p ->
                log.warn("   • {} — {} unidades (mín: {})", p.nome(), p.quantidadeEstoque(), p.estoqueMinimo()));
        }

        if (!alertas.vencidos().isEmpty()) {
            log.warn("🗓️  VENCIDOS ({}):", alertas.vencidos().size());
            alertas.vencidos().forEach(p ->
                log.warn("   • {} — venceu em {}", p.nome(), p.dataValidade()));
        }

        if (!alertas.validadeProxima().isEmpty()) {
            log.warn("⏰ VALIDADE PRÓXIMA ({}):", alertas.validadeProxima().size());
            alertas.validadeProxima().forEach(p ->
                log.warn("   • {} — vence em {}", p.nome(), p.dataValidade()));
        }

        log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /** Relatório matinal: todo dia às 08:00 */
    @Scheduled(cron = "0 0 8 * * *")
    public void relatorioMatinal() {
        AlertasEstoqueResponse alertas = produtoService.getAlertas();
        log.info("☀️  BOM DIA! Relatório de estoque: {} alertas pendentes " +
                 "({} zerados, {} baixo, {} vencidos, {} validade próxima)",
                alertas.totalAlertas(),
                alertas.estoqueZerado().size(),
                alertas.estoqueBaixo().size(),
                alertas.vencidos().size(),
                alertas.validadeProxima().size());
    }
}
