package com.mercadofacil.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadofacil.dto.request.AbrirCaixaRequest;
import com.mercadofacil.dto.request.FecharCaixaRequest;
import com.mercadofacil.dto.response.CaixaResponse;
import com.mercadofacil.dto.response.ResumoFechamentoCaixaResponse;
import com.mercadofacil.exception.CaixaException;
import com.mercadofacil.service.CaixaService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CaixaController.class)
@DisplayName("CaixaController — Testes de Integração")
class CaixaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CaixaService caixaService;

    private CaixaResponse caixaResponseMock;

    @BeforeEach
    void setUp() {
        caixaResponseMock = new CaixaResponse(
                1L, "ABERTO",
                new BigDecimal("100.00"), null,
                new BigDecimal("500.00"), new BigDecimal("200.00"),
                BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("700.00"), new BigDecimal("700.00"),
                null, "Operador", null,
                LocalDateTime.now(), null
        );
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /caixa/status com caixa aberto deve retornar aberto=true")
    void status_caixaAberto_retornaAberto() throws Exception {
        when(caixaService.hasCaixaAberto()).thenReturn(true);
        when(caixaService.getCaixaAtual()).thenReturn(caixaResponseMock);

        mockMvc.perform(get("/api/v1/caixa/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aberto").value(true))
                .andExpect(jsonPath("$.caixa.status").value("ABERTO"));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /caixa/status sem caixa aberto deve retornar aberto=false")
    void status_semCaixaAberto_retornaFechado() throws Exception {
        when(caixaService.hasCaixaAberto()).thenReturn(false);

        mockMvc.perform(get("/api/v1/caixa/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aberto").value(false));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /caixa/abrir deve abrir caixa e retornar 201")
    void abrir_semCaixaAberto_retorna201() throws Exception {
        when(caixaService.abrir(any())).thenReturn(caixaResponseMock);

        mockMvc.perform(post("/api/v1/caixa/abrir")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AbrirCaixaRequest(new BigDecimal("100.00")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.valorAbertura").value(100.00));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /caixa/abrir com caixa já aberto deve retornar 409")
    void abrir_caixaJaAberto_retorna409() throws Exception {
        when(caixaService.abrir(any())).thenThrow(CaixaException.caixaJaAberto());

        mockMvc.perform(post("/api/v1/caixa/abrir")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AbrirCaixaRequest(BigDecimal.ZERO))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("POST /caixa/fechar deve fechar e retornar resumo completo")
    void fechar_caixaAberto_retornaResumo() throws Exception {
        var resumo = new ResumoFechamentoCaixaResponse(
                caixaResponseMock,
                new BigDecimal("600.00"),
                new BigDecimal("600.00"),
                BigDecimal.ZERO,
                15L,
                new BigDecimal("46.67")
        );
        when(caixaService.fechar(any())).thenReturn(resumo);

        mockMvc.perform(post("/api/v1/caixa/fechar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FecharCaixaRequest(new BigDecimal("600.00"), "Normal"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeVendas").value(15))
                .andExpect(jsonPath("$.diferenca").value(0.00))
                .andExpect(jsonPath("$.ticketMedio").value(46.67));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /caixa/fechar por OPERADOR deve retornar 403")
    void fechar_semPermissao_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/caixa/fechar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FecharCaixaRequest(BigDecimal.ZERO, null))))
                .andExpect(status().isForbidden());
    }
}
