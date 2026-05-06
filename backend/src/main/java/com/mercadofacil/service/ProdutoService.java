package com.mercadofacil.service;

import com.mercadofacil.dto.request.AjusteEstoqueRequest;
import com.mercadofacil.dto.request.ProdutoRequest;
import com.mercadofacil.dto.response.AlertasEstoqueResponse;
import com.mercadofacil.dto.response.MovimentacaoEstoqueResponse;
import com.mercadofacil.dto.response.PageResponse;
import com.mercadofacil.dto.response.ProdutoResponse;
import com.mercadofacil.entity.Categoria;
import com.mercadofacil.entity.Produto;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.CategoriaRepository;
import com.mercadofacil.repository.MovimentacaoEstoqueRepository;
import com.mercadofacil.repository.ProdutoRepository;
import com.mercadofacil.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstoqueService estoqueService;
    private final AuditService auditService;

    // ─── Listagem e Busca ──────────────────────────────────────────────────────

    public PageResponse<ProdutoResponse> listar(String busca, Long categoriaId, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("nome").ascending());
        Page<ProdutoResponse> page = produtoRepository
                .buscarProdutos(busca, categoriaId, pageable)
                .map(ProdutoResponse::from);
        return PageResponse.from(page);
    }

    public ProdutoResponse buscarPorId(Long id) {
        return ProdutoResponse.from(findAtivoOrThrow(id));
    }

    public Optional<ProdutoResponse> buscarPorCodigoBarras(String codigo) {
        return produtoRepository.findByCodigoBarras(codigo)
                .filter(Produto::isAtivo)
                .map(ProdutoResponse::from);
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        validarPrecos(request);
        validarCodigoBarras(request.codigoBarras(), null);

        Categoria categoria = resolverCategoria(request.categoriaId());
        Produto produto = buildProduto(new Produto(), request, categoria);

        Produto salvo = produtoRepository.save(produto);
        auditService.produtoCriado(salvo.getId(), salvo.getNome());

        // Se já começa com estoque, registra entrada inicial
        if (salvo.getQuantidadeEstoque() > 0) {
            estoqueService.entradaEstoque(salvo, salvo.getQuantidadeEstoque(),
                    "Estoque inicial no cadastro", getUsuarioLogado());
        }

        log.info("✅ Produto criado: '{}' (ID: {})", salvo.getNome(), salvo.getId());
        return ProdutoResponse.from(salvo);
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = findAtivoOrThrow(id);
        validarPrecos(request);
        validarCodigoBarras(request.codigoBarras(), id);
        String nomeBefore = produto.getNome();

        Categoria categoria = resolverCategoria(request.categoriaId());
        buildProduto(produto, request, categoria);

        Produto salvo = produtoRepository.save(produto);
        auditService.produtoEditado(salvo.getId(), salvo.getNome(),
                "produto", nomeBefore, salvo.getNome());
        log.info("✏️  Produto atualizado: '{}' (ID: {})", salvo.getNome(), salvo.getId());
        return ProdutoResponse.from(salvo);
    }

    @Transactional
    public void desativar(Long id) {
        Produto produto = findAtivoOrThrow(id);
        produto.setAtivo(false);
        produtoRepository.save(produto);
        auditService.produtoDesativado(id, produto.getNome());
        log.info("🗑️  Produto desativado: '{}' (ID: {})", produto.getNome(), id);
    }

    // ─── Estoque ─────────────────────────────────────────────────────────────

    @Transactional
    public ProdutoResponse ajustarEstoque(Long id, AjusteEstoqueRequest request) {
        Produto produto = findAtivoOrThrow(id);
        Usuario operador = getUsuarioLogado();
        int antes = produto.getQuantidadeEstoque();


        switch (request.tipo()) {
            case ENTRADA ->
                estoqueService.entradaEstoque(produto, request.quantidade(), request.motivo(), operador);
            case AJUSTE_INVENTARIO, SAIDA_AJUSTE ->
                estoqueService.ajustarEstoque(produto,
                        calcularQuantidadeAjuste(produto, request), request.motivo(), operador);
            default -> throw new BusinessException("Tipo de ajuste inválido: " + request.tipo());
        }

        Produto salvo = produtoRepository.save(produto);
        auditService.estoqueAjustado(salvo.getId(), salvo.getNome(),
                antes, salvo.getQuantidadeEstoque(), request.motivo());
        return ProdutoResponse.from(salvo);
    }

    public PageResponse<MovimentacaoEstoqueResponse> listarMovimentacoes(Long produtoId, int pagina, int tamanho) {
        findAtivoOrThrow(produtoId); // valida existência
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending());
        return PageResponse.from(
                movimentacaoRepository.findByProdutoIdOrderByCriadoEmDesc(produtoId, pageable)
                        .map(MovimentacaoEstoqueResponse::from));
    }

    // ─── Alertas ─────────────────────────────────────────────────────────────

    public AlertasEstoqueResponse getAlertas() {
        LocalDate hoje = LocalDate.now();
        List<ProdutoResponse> baixo    = produtoRepository.findEstoqueBaixo().stream().map(ProdutoResponse::from).toList();
        List<ProdutoResponse> zerado   = produtoRepository.findEstoqueZerado().stream().map(ProdutoResponse::from).toList();
        List<ProdutoResponse> validade = produtoRepository.findValidadeProxima(hoje.plusDays(7)).stream().map(ProdutoResponse::from).toList();
        List<ProdutoResponse> vencidos = produtoRepository.findVencidos(hoje).stream().map(ProdutoResponse::from).toList();
        return AlertasEstoqueResponse.of(baixo, zerado, validade, vencidos);
    }

    public List<ProdutoResponse> listarEstoqueBaixo() {
        return produtoRepository.findEstoqueBaixo().stream().map(ProdutoResponse::from).toList();
    }

    public List<ProdutoResponse> listarVencidos() {
        return produtoRepository.findVencidos(LocalDate.now()).stream().map(ProdutoResponse::from).toList();
    }

    public List<ProdutoResponse> listarValidadeProxima(int dias) {
        return produtoRepository.findValidadeProxima(LocalDate.now().plusDays(dias))
                .stream().map(ProdutoResponse::from).toList();
    }

    // ─── Helpers Privados ────────────────────────────────────────────────────

    private Produto findAtivoOrThrow(Long id) {
        Produto p = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
        if (!p.isAtivo()) throw new ResourceNotFoundException("Produto", id);
        return p;
    }

    private Produto buildProduto(Produto produto, ProdutoRequest req, Categoria categoria) {
        produto.setCodigoBarras(req.codigoBarras());
        produto.setNome(req.nome());
        produto.setDescricao(req.descricao());
        produto.setCategoria(categoria);
        produto.setQuantidadeEstoque(req.quantidadeEstoque());
        produto.setEstoqueMinimo(req.estoqueMinimo() != null ? req.estoqueMinimo() : 10);
        produto.setPrecoCusto(req.precoCusto());
        produto.setPrecoVenda(req.precoVenda());
        produto.setDataValidade(req.dataValidade());
        produto.setAtivo(true);
        return produto;
    }

    private void validarPrecos(ProdutoRequest request) {
        if (request.precoVenda().compareTo(request.precoCusto()) < 0) {
            throw new BusinessException(
                "Preço de venda (R$ " + request.precoVenda() +
                ") não pode ser menor que o preço de custo (R$ " + request.precoCusto() + ").");
        }
    }

    private void validarCodigoBarras(String codigo, Long idAtual) {
        if (codigo == null || codigo.isBlank()) return;
        produtoRepository.findByCodigoBarras(codigo).ifPresent(p -> {
            if (!p.getId().equals(idAtual)) {
                throw new BusinessException("Código de barras '" + codigo + "' já está em uso.");
            }
        });
    }

    private Categoria resolverCategoria(Long categoriaId) {
        if (categoriaId == null) return null;
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", categoriaId));
    }

    private int calcularQuantidadeAjuste(Produto produto, AjusteEstoqueRequest request) {
        // Para ajuste inventário: a quantidade no request É o novo total
        return request.quantidade();
    }

    private Usuario getUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email).orElse(null);
    }
}
