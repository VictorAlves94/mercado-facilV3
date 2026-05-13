package com.mercadofacil.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadofacil.dto.request.AbrirCaixaRequest;
import com.mercadofacil.dto.request.FecharCaixaRequest;
import com.mercadofacil.dto.request.MovimentacaoCaixaRequest;
import com.mercadofacil.dto.response.CaixaResponse;
import com.mercadofacil.dto.response.MovimentacaoCaixaResponse;
import com.mercadofacil.dto.response.ResumoFechamentoCaixaResponse;
import com.mercadofacil.entity.MovimentacaoCaixa;
import com.mercadofacil.exception.CaixaException;
import com.mercadofacil.service.CaixaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@Import(com.mercadofacil.config.SecurityConfig.class)
@WebMvcTest(CaixaController.class)
@DisplayName("CaixaController — Testes de Integração Web")
class CaixaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CaixaService caixaService;
    @MockBean com.mercadofacil.security.JwtService jwtService;
    @MockBean com.mercadofacil.repository.UsuarioRepository usuarioRepository;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CaixaResponse caixaResponseFake() {
        return new CaixaResponse(
                1L,                          // id
                "ABERTO",                    // status
                new BigDecimal("500.00"),    // valorAbertura
                null,                        // valorFechamento
                new BigDecimal("1200.00"),   // totalDinheiro
                new BigDecimal("300.00"),    // totalPix
                new BigDecimal("200.00"),    // totalCartaoDebito
                new BigDecimal("100.00"),    // totalCartaoCredito
                new BigDecimal("2300.00"),   // totalVendas
                new BigDecimal("2300.00"),   // totalGeral
                null,                        // observacaoFechamento
                "Operador Teste",            // abertoPorNome
                null,                        // fechadoPorNome
                LocalDateTime.now(),         // abertoEm
                null                         // fechadoEm
        );
    }

    // ── GET /status ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /status — caixa aberto retorna aberto=true e dados do caixa")
    void status_caixaAberto_retornaAbertoEDados() throws Exception {
        when(caixaService.hasCaixaAberto()).thenReturn(true);
        when(caixaService.getCaixaAtual()).thenReturn(caixaResponseFake());

        mockMvc.perform(get("/api/v1/caixa/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aberto").value(true))
                .andExpect(jsonPath("$.caixa").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /status — caixa fechado retorna apenas aberto=false")
    void status_caixaFechado_retornaApenasAberto() throws Exception {
        when(caixaService.hasCaixaAberto()).thenReturn(false);

        mockMvc.perform(get("/api/v1/caixa/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aberto").value(false))
                .andExpect(jsonPath("$.caixa").doesNotExist());
    }

    // ── GET /atual ────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /atual — retorna caixa aberto com status 200")
    void getCaixaAtual_retornaCaixaAberto() throws Exception {
        when(caixaService.getCaixaAtual()).thenReturn(caixaResponseFake());

        mockMvc.perform(get("/api/v1/caixa/atual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /atual — sem caixa aberto retorna 409")
    void getCaixaAtual_semCaixaAberto_retorna409() throws Exception {
        when(caixaService.getCaixaAtual()).thenThrow(CaixaException.semCaixaAberto());

        mockMvc.perform(get("/api/v1/caixa/atual"))
                .andExpect(status().isConflict());
    }

    // ── GET /historico ────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /historico — retorna lista de caixas")
    void listarHistorico_retornaLista() throws Exception {
        when(caixaService.listarHistorico()).thenReturn(List.of(caixaResponseFake()));

        mockMvc.perform(get("/api/v1/caixa/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /historico — histórico vazio retorna lista vazia")
    void listarHistorico_vazio_retornaListaVazia() throws Exception {
        when(caixaService.listarHistorico()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/caixa/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /{id} ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /{id} — caixa existente retorna 200")
    void buscarPorId_existente_retorna200() throws Exception {
        when(caixaService.buscarPorId(1L)).thenReturn(caixaResponseFake());

        mockMvc.perform(get("/api/v1/caixa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /{id} — caixa inexistente retorna 404")
    void buscarPorId_inexistente_retorna404() throws Exception {
        when(caixaService.buscarPorId(99L))
                .thenThrow(new com.mercadofacil.exception.ResourceNotFoundException("Caixa", 99L));

        mockMvc.perform(get("/api/v1/caixa/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /abrir ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /abrir — abre caixa com sucesso e retorna 201")
    void abrir_sucesso_retorna201() throws Exception {
        var request = new AbrirCaixaRequest(new BigDecimal("500.00"));
        when(caixaService.abrir(any())).thenReturn(caixaResponseFake());

        mockMvc.perform(post("/api/v1/caixa/abrir")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /abrir — caixa já aberto retorna 409")
    void abrir_caixaJaAberto_retorna409() throws Exception {
        var request = new AbrirCaixaRequest(new BigDecimal("500.00"));
        when(caixaService.abrir(any())).thenThrow(CaixaException.caixaJaAberto());

        mockMvc.perform(post("/api/v1/caixa/abrir")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── POST /fechar ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /fechar — fecha caixa com sucesso (ADMIN)")
    void fechar_comAdmin_retorna200() throws Exception {
        var request = new FecharCaixaRequest(new BigDecimal("1700.00"), "Fechamento normal");
        var resumo = new ResumoFechamentoCaixaResponse(
                caixaResponseFake(),
                new BigDecimal("1700.00"),
                new BigDecimal("1700.00"),
                BigDecimal.ZERO,
                10L,
                new BigDecimal("170.00")
        );
        when(caixaService.fechar(any())).thenReturn(resumo);

        mockMvc.perform(post("/api/v1/caixa/fechar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeVendas").value(10))
                .andExpect(jsonPath("$.diferenca").value(0));
    }


    // ── POST /movimentacao ────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /movimentacao — sangria registrada retorna 201")
    void registrarMovimentacao_sangria_retorna201() throws Exception {
        var request = new MovimentacaoCaixaRequest(
                MovimentacaoCaixa.TipoMovimentacaoCaixa.SANGRIA,
                new BigDecimal("100.00"),
                "Retirada para cofre"
        );
        var response = new MovimentacaoCaixaResponse(
                1L,
                MovimentacaoCaixa.TipoMovimentacaoCaixa.SANGRIA.name(),
                new BigDecimal("100.00"),
                "Retirada para cofre",
                "Operador Teste",
                LocalDateTime.now()
        );
        when(caixaService.registrarMovimentacao(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/caixa/movimentacao")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("SANGRIA"))
                .andExpect(jsonPath("$.valor").value(100.00));
    }

    // ── GET /{id}/movimentacoes ───────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /{id}/movimentacoes — retorna lista de movimentações")
    void listarMovimentacoes_retornaLista() throws Exception {
        var mov = new MovimentacaoCaixaResponse(
                1L,
                MovimentacaoCaixa.TipoMovimentacaoCaixa.SUPRIMENTO.name(),
                new BigDecimal("200.00"),
                "Troco inicial",
                "Operador Teste",
                LocalDateTime.now()
        );
        when(caixaService.listarMovimentacoes(1L)).thenReturn(List.of(mov));

        mockMvc.perform(get("/api/v1/caixa/1/movimentacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("SUPRIMENTO"));
    }
}