package com.mercadofacil.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadofacil.dto.request.FiadoRequest;
import com.mercadofacil.dto.request.LancamentoFiadoRequest;
import com.mercadofacil.dto.response.FiadoResponse;
import com.mercadofacil.dto.response.LancamentoFiadoResponse;
import com.mercadofacil.entity.LancamentoFiado;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.service.FiadoService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FiadoController.class)
@DisplayName("FiadoController — Testes de Integração")
class FiadoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean FiadoService fiadoService;

    private FiadoResponse fiadoMock;

    @BeforeEach
    void setUp() {
        fiadoMock = new FiadoResponse(1L, "João Silva", "61999999999",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "ATIVO", LocalDate.now(), "Admin", LocalDateTime.now());
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /fiado deve listar fiados ativos")
    void listar_retornaFiadosAtivos() throws Exception {
        when(fiadoService.listarAtivos()).thenReturn(List.of(fiadoMock));

        mockMvc.perform(get("/api/v1/fiado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCliente").value("João Silva"))
                .andExpect(jsonPath("$[0].saldoDevedor").value(50.00));
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /fiado deve criar fiado com status 201")
    void criar_clienteNovo_retorna201() throws Exception {
        when(fiadoService.criar(any())).thenReturn(fiadoMock);
        var req = new FiadoRequest("João Silva", "61999999999", new BigDecimal("200.00"));

        mockMvc.perform(post("/api/v1/fiado")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /fiado com nome vazio deve retornar 400")
    void criar_nomeVazio_retorna400() throws Exception {
        var req = new FiadoRequest("", null, null);

        mockMvc.perform(post("/api/v1/fiado")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nomeCliente").exists());
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /fiado/{id}/lancamentos deve registrar débito")
    void lancar_debito_retorna201() throws Exception {
        var lancamento = new LancamentoFiadoResponse(1L, 1L, "João Silva",
                "DEBITO", new BigDecimal("30.00"), "Compras do dia",
                null, "Admin", LocalDateTime.now());
        when(fiadoService.lancar(eq(1L), any())).thenReturn(lancamento);

        var req = new LancamentoFiadoRequest(LancamentoFiado.TipoLancamento.DEBITO,
                new BigDecimal("30.00"), "Compras do dia");

        mockMvc.perform(post("/api/v1/fiado/1/lancamentos")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("DEBITO"))
                .andExpect(jsonPath("$.valor").value(30.00));
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /fiado/{id}/lancamentos acima do limite deve retornar 422")
    void lancar_acimaDoLimite_retorna422() throws Exception {
        when(fiadoService.lancar(eq(1L), any()))
                .thenThrow(new BusinessException("Lançamento excede o limite de crédito."));

        var req = new LancamentoFiadoRequest(LancamentoFiado.TipoLancamento.DEBITO,
                new BigDecimal("999.00"), "Compra enorme");

        mockMvc.perform(post("/api/v1/fiado/1/lancamentos")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("limite")));
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /fiado/{id}/bloquear deve bloquear cliente")
    void bloquear_fiadoAtivo_retornaFiadoBloqueado() throws Exception {
        var bloqueado = new FiadoResponse(1L, "João Silva", "61999999999",
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                "BLOQUEADO", LocalDate.now(), "Admin", LocalDateTime.now());
        when(fiadoService.bloquear(1L)).thenReturn(bloqueado);

        mockMvc.perform(post("/api/v1/fiado/1/bloquear").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOQUEADO"));
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /fiado/{id}/quitar deve quitar o fiado")
    void quitar_fiadoComSaldo_retornaQuitado() throws Exception {
        var quitado = new FiadoResponse(1L, "João Silva", "61999999999",
                BigDecimal.ZERO, new BigDecimal("200.00"),
                "QUITADO", LocalDate.now(), "Admin", LocalDateTime.now());
        when(fiadoService.quitar(1L)).thenReturn(quitado);

        mockMvc.perform(post("/api/v1/fiado/1/quitar").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUITADO"))
                .andExpect(jsonPath("$.saldoDevedor").value(0.00));
    }
}
