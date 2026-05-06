package com.mercadofacil.service;
import com.mercadofacil.entity.AuditLog;
import com.mercadofacil.entity.AuditLog.TipoAcao;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.repository.AuditLogRepository;
import com.mercadofacil.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UsuarioRepository usuarioRepository;

    // ── Método genérico (todos os outros chamam este) ─────────────
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(TipoAcao acao, String entidade, Long entidadeId,
                          String entidadeReferencia, String descricao,
                          String valorAnterior, String valorPosterior) {

        Usuario usuario = getUsuarioAtual();

        AuditLog registro = AuditLog.builder()
                .usuario(usuario)
                .usuarioNome(usuario != null ? usuario.getNome() : "Sistema")
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .entidadeReferencia(entidadeReferencia)
                .descricao(descricao)
                .valorAnterior(valorAnterior)
                .valorPosterior(valorPosterior)
                .build();

        auditLogRepository.save(registro);

        log.info("📋 AUDIT [{}] {} — {}",
                acao, registro.getUsuarioNome(), descricao);
    }

    // ── Atalhos semânticos ────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void vendaCriada(Long id, String numero, String total) {
        String nome = getUsuarioNome();
        registrar(TipoAcao.VENDA_CRIADA, "Venda", id, numero,
                nome + " registrou a venda " + numero + " — Total: R$ " + total,
                null, "R$ " + total);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void vendaCancelada(Long id, String numero, String motivo) {
        String nome = getUsuarioNome();
        registrar(TipoAcao.VENDA_CANCELADA, "Venda", id, numero,
                nome + " cancelou a venda " + numero + " — Motivo: " + motivo,
                "FINALIZADA", "CANCELADA — " + motivo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void produtoCriado(Long id, String nomeProduto) {
        String nome = getUsuarioNome();
        registrar(TipoAcao.PRODUTO_CRIADO, "Produto", id, nomeProduto,
                nome + " cadastrou o produto " + nomeProduto,
                null, nomeProduto);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void produtoEditado(Long id, String nomeProduto,
                               String campo, String antes, String depois) {
        String nome = getUsuarioNome();
        registrar(TipoAcao.PRODUTO_EDITADO, "Produto", id, nomeProduto,
                nome + " alterou " + nomeProduto + " — " + campo + ": " + antes + " → " + depois,
                antes, depois);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void produtoDesativado(Long id, String nomeProduto) {
        String nome = getUsuarioNome();
        registrar(TipoAcao.PRODUTO_DESATIVADO, "Produto", id, nomeProduto,
                nome + " desativou o produto " + nomeProduto,
                "ATIVO", "INATIVO");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void estoqueAjustado(Long id, String nomeProduto,
                                int antes, int depois, String motivo) {
        String nome = getUsuarioNome();
        TipoAcao acao = depois > antes ? TipoAcao.ESTOQUE_ENTRADA : TipoAcao.ESTOQUE_AJUSTE;
        registrar(acao, "Produto", id, nomeProduto,
                nome + " alterou estoque de " + nomeProduto +
                        " de " + antes + " para " + depois + " unidades — " + motivo,
                String.valueOf(antes), String.valueOf(depois));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void caixaAberto(Long id, String valorAbertura) {
        String nome = getUsuarioNome();
        registrar(TipoAcao.CAIXA_ABERTO, "Caixa", id, "#" + id,
                nome + " abriu o caixa #" + id + " — Fundo: R$ " + valorAbertura,
                "FECHADO", "ABERTO — Fundo: R$ " + valorAbertura);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void caixaFechado(Long id, String totalVendas, String diferenca) {
        String nome = getUsuarioNome();
        registrar(TipoAcao.CAIXA_FECHADO, "Caixa", id, "#" + id,
                nome + " fechou o caixa #" + id +
                        " — Total vendas: R$ " + totalVendas + " | Diferença: R$ " + diferenca,
                "ABERTO", "FECHADO — Total: R$ " + totalVendas);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void despesaLancada(Long id, String descricao, String valor) {
        String nome = getUsuarioNome();
        registrar(TipoAcao.DESPESA_LANCADA, "Despesa", id, descricao,
                nome + " lançou despesa " + descricao + " — R$ " + valor,
                null, "R$ " + valor);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void despesaExcluida(Long id, String descricao, String valor) {
        String nome = getUsuarioNome();
        registrar(TipoAcao.DESPESA_EXCLUIDA, "Despesa", id, descricao,
                nome + " excluiu despesa " + descricao + " — R$ " + valor,
                "R$ " + valor, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fiadoDebito(Long id, String cliente, String valor) {
        String nome = getUsuarioNome();
        registrar(TipoAcao.FIADO_DEBITO, "Fiado", id, cliente,
                nome + " lançou fiado para " + cliente + " — R$ " + valor,
                null, "+ R$ " + valor);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fiadoPagamento(Long id, String cliente, String valor) {
        String nome = getUsuarioNome();
        registrar(TipoAcao.FIADO_PAGAMENTO, "Fiado", id, cliente,
                nome + " registrou pagamento de " + cliente + " — R$ " + valor,
                null, "− R$ " + valor);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void loginRealizado(String email) {
        auditLogRepository.save(AuditLog.builder()
                .usuarioNome(email)
                .acao(TipoAcao.LOGIN_REALIZADO)
                .entidade("Auth")
                .descricao("Login realizado: " + email)
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void loginFalhou(String email) {
        auditLogRepository.save(AuditLog.builder()
                .usuarioNome(email)
                .acao(TipoAcao.LOGIN_FALHOU)
                .entidade("Auth")
                .descricao("Tentativa de login falhou: " + email)
                .build());
    }

    // ── Helpers privados ──────────────────────────────────────────

    private Usuario getUsuarioAtual() {
        try {
            String email = SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            return usuarioRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String getUsuarioNome() {
        Usuario u = getUsuarioAtual();
        return u != null ? u.getNome() : "Sistema";
    }

}
