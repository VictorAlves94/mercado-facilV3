package com.mercadofacil.service;

import com.mercadofacil.dto.request.AjusteEstoqueRequest;
import com.mercadofacil.dto.request.ProdutoRequest;
import com.mercadofacil.dto.response.AlertasEstoqueResponse;
import com.mercadofacil.dto.response.ProdutoResponse;
import com.mercadofacil.entity.Categoria;
import com.mercadofacil.entity.MovimentacaoEstoque;
import com.mercadofacil.entity.Produto;
import com.mercadofacil.entity.Usuario;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.CategoriaRepository;
import com.mercadofacil.repository.MovimentacaoEstoqueRepository;
import com.mercadofacil.repository.ProdutoRepository;
import com.mercadofacil.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService — Testes Unitários")
class ProdutoServiceTest {

    @Mock ProdutoRepository produtoRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock MovimentacaoEstoqueRepository movimentacaoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock EstoqueService estoqueService;
    @InjectMocks ProdutoService produtoService;

    private Produto produtoMock;
    private Categoria categoriaMock;
    private ProdutoRequest requestValido;

    @BeforeEach
    void setUp() {
        categoriaMock = Categoria.builder().id(1L).nome("Bebidas").build();

        produtoMock = Produto.builder()
                .id(1L).nome("Coca-Cola 2L")
                .codigoBarras("7891000100103")
                .categoria(categoriaMock)
                .quantidadeEstoque(48).estoqueMinimo(10)
                .precoCusto(new BigDecimal("6.50"))
                .precoVenda(new BigDecimal("9.99"))
                .ativo(true)
                .build();

        requestValido = new ProdutoRequest(
                "7891000100103", "Coca-Cola 2L", "Refrigerante 2L",
                1L, 48, 10,
                new BigDecimal("6.50"), new BigDecimal("9.99"),
                LocalDate.now().plusMonths(6));

        // Mock SecurityContext para getUsuarioLogado()
        var auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@mercadofacil.com");
        var ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(
                Usuario.builder().id(1L).nome("Admin").email("admin@mercadofacil.com").build()));
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @Nested @DisplayName("Criar Produto")
    class CriarProduto {

        @Test
        @DisplayName("Deve criar produto com dados válidos")
        void criar_dadosValidos_retornaProduto() {
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaMock));
            when(produtoRepository.findByCodigoBarras(anyString())).thenReturn(Optional.empty());
            when(produtoRepository.save(any())).thenReturn(produtoMock);

            ProdutoResponse resp = produtoService.criar(requestValido);

            assertThat(resp.nome()).isEqualTo("Coca-Cola 2L");
            assertThat(resp.precoVenda()).isEqualByComparingTo("9.99");
            verify(produtoRepository).save(any());
            verify(estoqueService).entradaEstoque(any(), eq(48), anyString(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção se preço de venda menor que custo")
        void criar_precoVendaMenorQueCusto_lancaBusinessException() {
            var requestInvalido = new ProdutoRequest(
                    null, "Produto X", null, null, 10, 5,
                    new BigDecimal("10.00"), new BigDecimal("5.00"), null);

            assertThatThrownBy(() -> produtoService.criar(requestInvalido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Preço de venda");
        }

        @Test
        @DisplayName("Deve lançar exceção se código de barras duplicado")
        void criar_codigoBarrasDuplicado_lancaBusinessException() {
            when(produtoRepository.findByCodigoBarras("7891000100103"))
                    .thenReturn(Optional.of(Produto.builder().id(99L).build()));
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaMock));

            assertThatThrownBy(() -> produtoService.criar(requestValido))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Código de barras");
        }
    }

    @Nested @DisplayName("Busca e Listagem")
    class BuscaListagem {

        @Test
        @DisplayName("Deve buscar produto por código de barras existente")
        void buscarPorCodigoBarras_existente_retornaProduto() {
            when(produtoRepository.findByCodigoBarras("7891000100103"))
                    .thenReturn(Optional.of(produtoMock));

            var result = produtoService.buscarPorCodigoBarras("7891000100103");

            assertThat(result).isPresent();
            assertThat(result.get().codigoBarras()).isEqualTo("7891000100103");
        }

        @Test
        @DisplayName("Deve retornar empty se código de barras não encontrado")
        void buscarPorCodigoBarras_inexistente_retornaEmpty() {
            when(produtoRepository.findByCodigoBarras("0000")).thenReturn(Optional.empty());
            assertThat(produtoService.buscarPorCodigoBarras("0000")).isEmpty();
        }

        @Test
        @DisplayName("Listar deve retornar página com produtos ativos")
        void listar_retornaPaginaComProdutos() {
            when(produtoRepository.buscarProdutos(any(), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(produtoMock)));

            var page = produtoService.listar(null, null, 0, 20);

            assertThat(page.content()).hasSize(1);
            assertThat(page.totalElementos()).isEqualTo(1);
        }
    }

    @Nested @DisplayName("Ajuste de Estoque")
    class AjusteEstoque {

        @Test
        @DisplayName("Entrada de estoque deve chamar EstoqueService corretamente")
        void ajustarEstoque_entrada_chamaEstoqueService() {
            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoMock));
            when(produtoRepository.save(any())).thenReturn(produtoMock);

            var req = new AjusteEstoqueRequest(20, MovimentacaoEstoque.TipoMovimentacao.ENTRADA, "Reposição");
            produtoService.ajustarEstoque(1L, req);

            verify(estoqueService).entradaEstoque(eq(produtoMock), eq(20), eq("Reposição"), any());
        }

        @Test
        @DisplayName("Ajuste de produto inexistente deve lançar ResourceNotFoundException")
        void ajustarEstoque_produtoInexistente_lancaException() {
            when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

            var req = new AjusteEstoqueRequest(10, MovimentacaoEstoque.TipoMovimentacao.ENTRADA, "Teste");
            assertThatThrownBy(() -> produtoService.ajustarEstoque(999L, req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("Alertas de Estoque")
    class AlertasEstoque {

        @Test
        @DisplayName("getAlertas deve agregar todos os tipos de alerta")
        void getAlertas_retornaTodosAlertas() {
            Produto prodBaixo = Produto.builder().id(2L).nome("Açúcar")
                    .quantidadeEstoque(5).estoqueMinimo(10)
                    .precoCusto(BigDecimal.ONE).precoVenda(BigDecimal.TEN).ativo(true).build();
            Produto prodZerado = Produto.builder().id(3L).nome("Sal")
                    .quantidadeEstoque(0).estoqueMinimo(10)
                    .precoCusto(BigDecimal.ONE).precoVenda(BigDecimal.TEN).ativo(true).build();

            when(produtoRepository.findEstoqueBaixo()).thenReturn(List.of(prodBaixo));
            when(produtoRepository.findEstoqueZerado()).thenReturn(List.of(prodZerado));
            when(produtoRepository.findValidadeProxima(any())).thenReturn(List.of());
            when(produtoRepository.findVencidos(any())).thenReturn(List.of());

            AlertasEstoqueResponse alertas = produtoService.getAlertas();

            assertThat(alertas.estoqueBaixo()).hasSize(1);
            assertThat(alertas.estoqueZerado()).hasSize(1);
            assertThat(alertas.totalAlertas()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Sem alertas deve retornar listas vazias com total zero")
        void getAlertas_semAlertas_retornaZero() {
            when(produtoRepository.findEstoqueBaixo()).thenReturn(List.of());
            when(produtoRepository.findEstoqueZerado()).thenReturn(List.of());
            when(produtoRepository.findValidadeProxima(any())).thenReturn(List.of());
            when(produtoRepository.findVencidos(any())).thenReturn(List.of());

            AlertasEstoqueResponse alertas = produtoService.getAlertas();

            assertThat(alertas.totalAlertas()).isZero();
        }
    }

    @Nested @DisplayName("Desativar Produto")
    class DesativarProduto {

        @Test
        @DisplayName("Deve desativar produto existente")
        void desativar_produtoExistente_setaAtivoFalse() {
            when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoMock));
            when(produtoRepository.save(any())).thenReturn(produtoMock);

            produtoService.desativar(1L);

            assertThat(produtoMock.isAtivo()).isFalse();
            verify(produtoRepository).save(produtoMock);
        }

        @Test
        @DisplayName("Desativar produto inexistente deve lançar ResourceNotFoundException")
        void desativar_inexistente_lancaException() {
            when(produtoRepository.findById(999L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> produtoService.desativar(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }
}
