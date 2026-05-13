package com.mercadofacil.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadofacil.dto.request.ProdutoRequest;
import com.mercadofacil.dto.response.AlertasEstoqueResponse;
import com.mercadofacil.dto.response.PageResponse;
import com.mercadofacil.dto.response.ProdutoResponse;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoController.class)
@DisplayName("ProdutoController — Testes de Integração")
class ProdutoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProdutoService produtoService;
    @MockBean com.mercadofacil.security.JwtService jwtService;
    @MockBean com.mercadofacil.repository.UsuarioRepository usuarioRepository;
    @MockBean org.springframework.security.authentication.AuthenticationProvider authenticationProvider;


    private ProdutoResponse produtoResponse;

    @BeforeEach
    void setUp() {
        produtoResponse = new ProdutoResponse(
            1L, "7891000100103", "Coca-Cola 2L", "Refrigerante 2L",
            1L, "Bebidas", 48, 10,
            new BigDecimal("6.50"), new BigDecimal("9.99"),
            new BigDecimal("34.93"), null, true,
            false, false, false, false,
            LocalDateTime.now(), null
        );
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /produtos deve retornar lista paginada")
    void listar_retornaPaginaComProdutos() throws Exception {
        var page = new PageResponse<>(List.of(produtoResponse), 0, 20, 1L, 1, true, true);
        when(produtoService.listar(any(),any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Coca-Cola 2L"))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.content[0].precoVenda").value(9.99));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /produtos/{id} deve retornar produto por id")
    void buscarPorId_existente_retornaProduto() throws Exception {
        when(produtoService.buscarPorId(1L)).thenReturn(produtoResponse);

        mockMvc.perform(get("/api/v1/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigoBarras").value("7891000100103"));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /produtos/{id} inexistente deve retornar 404")
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(produtoService.buscarPorId(999L)).thenThrow(new ResourceNotFoundException("Produto", 999L));

        mockMvc.perform(get("/api/v1/produtos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto não encontrado(a) com ID: 999"));
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("POST /produtos com dados válidos deve criar produto com status 201")
    void criar_dadosValidos_retorna201() throws Exception {
        var request = new ProdutoRequest(
                "7891000100103", "Coca-Cola 2L", "Refrigerante",
                1L, 48, 10,
                new BigDecimal("6.50"), new BigDecimal("9.99"), null);
        when(produtoService.criar(any())).thenReturn(produtoResponse);

        mockMvc.perform(post("/api/v1/produtos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Coca-Cola 2L"));
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("POST /produtos com nome vazio deve retornar 400 com erros de validação")
    void criar_nomVazio_retorna400() throws Exception {
        var requestInvalido = new ProdutoRequest(
                null, "", null, null, 0, 10,
                BigDecimal.ONE, BigDecimal.TEN, null);

        mockMvc.perform(post("/api/v1/produtos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").exists());
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /produtos/alertas deve retornar alertas de estoque")
    void getAlertas_retornaAlertas() throws Exception {
        var alertas = AlertasEstoqueResponse.of(
                List.of(produtoResponse), List.of(), List.of(), List.of());
        when(produtoService.getAlertas()).thenReturn(alertas);

        mockMvc.perform(get("/api/v1/produtos/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAlertas").value(1))
                .andExpect(jsonPath("$.estoqueBaixo[0].nome").value("Coca-Cola 2L"));
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("DELETE /produtos/{id} deve desativar produto")
    void desativar_produtoExistente_retorna204() throws Exception {
        doNothing().when(produtoService).desativar(1L);

        mockMvc.perform(delete("/api/v1/produtos/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(produtoService).desativar(1L);
    }
}
