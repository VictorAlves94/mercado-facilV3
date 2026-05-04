package com.mercadofacil.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadofacil.dto.request.CancelarVendaRequest;
import com.mercadofacil.dto.request.ItemVendaRequest;
import com.mercadofacil.dto.request.VendaRequest;
import com.mercadofacil.dto.response.VendaResponse;
import com.mercadofacil.entity.Venda;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.CaixaException;
import com.mercadofacil.service.VendaService;
import org.junit.jupiter.api.*;
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

@WebMvcTest(VendaController.class)
@DisplayName("VendaController — Testes de Integração")
class VendaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean VendaService vendaService;

    private VendaResponse vendaResponseMock;

    @BeforeEach
    void setUp() {
        vendaResponseMock = new VendaResponse(
                1L, "V202506010001", 1L, "Operador",
                "DINHEIRO", "FINALIZADA",
                new BigDecimal("19.98"), BigDecimal.ZERO,
                new BigDecimal("19.98"), new BigDecimal("30.00"),
                new BigDecimal("10.02"), null, List.of(),
                LocalDateTime.now(), null
        );
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /vendas com dados válidos deve criar venda com status 201")
    void registrar_dadosValidos_retorna201() throws Exception {
        when(vendaService.registrar(any())).thenReturn(vendaResponseMock);

        var request = new VendaRequest(
                Venda.FormaPagamento.DINHEIRO,
                List.of(new ItemVendaRequest(1L, 2, BigDecimal.ZERO)),
                BigDecimal.ZERO, new BigDecimal("30.00")
        );

        mockMvc.perform(post("/api/v1/vendas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroVenda").value("V202506010001"))
                .andExpect(jsonPath("$.status").value("FINALIZADA"))
                .andExpect(jsonPath("$.valorTroco").value(10.02));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /vendas sem caixa aberto deve retornar 409")
    void registrar_semCaixaAberto_retorna409() throws Exception {
        when(vendaService.registrar(any())).thenThrow(CaixaException.semCaixaAberto());

        var request = new VendaRequest(
                Venda.FormaPagamento.PIX,
                List.of(new ItemVendaRequest(1L, 1, BigDecimal.ZERO)),
                BigDecimal.ZERO, null
        );

        mockMvc.perform(post("/api/v1/vendas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("caixa")));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /vendas sem itens deve retornar 400")
    void registrar_semItens_retorna400() throws Exception {
        var request = new VendaRequest(
                Venda.FormaPagamento.DINHEIRO, List.of(),
                BigDecimal.ZERO, new BigDecimal("10.00")
        );

        mockMvc.perform(post("/api/v1/vendas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /vendas/{id} deve retornar venda com itens")
    void buscarPorId_existente_retornaVenda() throws Exception {
        when(vendaService.buscarPorId(1L)).thenReturn(vendaResponseMock);

        mockMvc.perform(get("/api/v1/vendas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formaPagamento").value("DINHEIRO"))
                .andExpect(jsonPath("$.valorTotal").value(19.98));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /vendas/{id}/cancelar deve cancelar e retornar venda cancelada")
    void cancelar_vendaExistente_retornaVendaCancelada() throws Exception {
        var cancelada = new VendaResponse(
                1L, "V202506010001", 1L, "Operador",
                "DINHEIRO", "CANCELADA",
                new BigDecimal("19.98"), BigDecimal.ZERO,
                new BigDecimal("19.98"), new BigDecimal("30.00"),
                new BigDecimal("10.02"), "Produto errado", List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(vendaService.cancelar(eq(1L), any())).thenReturn(cancelada);

        mockMvc.perform(post("/api/v1/vendas/1/cancelar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CancelarVendaRequest("Produto errado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.motivoCancelamento").value("Produto errado"));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST cancelar já cancelada deve retornar 422")
    void cancelar_jaExistenteCancelada_retorna422() throws Exception {
        when(vendaService.cancelar(eq(1L), any()))
                .thenThrow(new BusinessException("Venda já está cancelada."));

        mockMvc.perform(post("/api/v1/vendas/1/cancelar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CancelarVendaRequest("Duplicado"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /vendas/hoje deve retornar lista de vendas do dia")
    void listarHoje_retornaVendasDoDia() throws Exception {
        when(vendaService.listarHoje()).thenReturn(List.of(vendaResponseMock));

        mockMvc.perform(get("/api/v1/vendas/hoje"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroVenda").value("V202506010001"));
    }
}
