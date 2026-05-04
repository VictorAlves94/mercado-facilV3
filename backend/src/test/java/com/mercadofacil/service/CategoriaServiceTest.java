package com.mercadofacil.service;

import com.mercadofacil.dto.response.CategoriaResponse;
import com.mercadofacil.entity.Categoria;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.CategoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaService — Testes Unitários")
class CategoriaServiceTest {

    @Mock CategoriaRepository categoriaRepository;
    @InjectMocks CategoriaService categoriaService;

    @Nested @DisplayName("Criar Categoria")
    class CriarCategoria {

        @Test
        @DisplayName("Deve criar categoria com nome único")
        void criar_nomeUnico_retornaCategoria() {
            when(categoriaRepository.existsByNome("Bebidas")).thenReturn(false);
            var cat = Categoria.builder().id(1L).nome("Bebidas").descricao("desc").build();
            when(categoriaRepository.save(any())).thenReturn(cat);

            CategoriaResponse resp = categoriaService.criar("Bebidas", "desc");

            assertThat(resp.nome()).isEqualTo("Bebidas");
            assertThat(resp.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção se nome já existe")
        void criar_nomeDuplicado_lancaException() {
            when(categoriaRepository.existsByNome("Bebidas")).thenReturn(true);

            assertThatThrownBy(() -> categoriaService.criar("Bebidas", "desc"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Bebidas");
        }
    }

    @Nested @DisplayName("Excluir Categoria")
    class ExcluirCategoria {

        @Test
        @DisplayName("Deve excluir categoria sem produtos")
        void excluir_semProdutos_deleta() {
            var cat = Categoria.builder().id(1L).nome("Bebidas").produtos(List.of()).build();
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(cat));

            categoriaService.excluir(1L);

            verify(categoriaRepository).delete(cat);
        }

        @Test
        @DisplayName("Não deve excluir categoria com produtos vinculados")
        void excluir_comProdutos_lancaException() {
            var produto = new com.mercadofacil.entity.Produto();
            var cat = Categoria.builder().id(1L).nome("Bebidas").produtos(List.of(produto)).build();
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(cat));

            assertThatThrownBy(() -> categoriaService.excluir(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("produtos vinculados");
        }

        @Test
        @DisplayName("Deve lançar exceção se categoria não existe")
        void excluir_inexistente_lancaException() {
            when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoriaService.excluir(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Test
    @DisplayName("listarTodas deve retornar lista ordenada por nome")
    void listarTodas_retornaOrdenada() {
        var cats = List.of(
            Categoria.builder().id(1L).nome("Bebidas").build(),
            Categoria.builder().id(2L).nome("Laticínios").build()
        );
        when(categoriaRepository.findAllByOrderByNomeAsc()).thenReturn(cats);

        List<CategoriaResponse> result = categoriaService.listarTodas();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).nome()).isEqualTo("Bebidas");
    }
}
