package com.mercadofacil.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadofacil.dto.request.DespesaRequest;
import com.mercadofacil.dto.response.*;
import com.mercadofacil.entity.Despesa;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.service.FinanceiroService;
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

@WebMvcTest(FinanceiroController.class)
@DisplayName("FinanceiroController — Testes de Integração")
class FinanceiroControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean FinanceiroService financeiroService;

    @MockBean com.mercadofacil.security.JwtService jwtService;
    @MockBean com.mercadofacil.repository.UsuarioRepository usuarioRepository;
    @MockBean org.springframework.security.authentication.AuthenticationProvider authenticationProvider;


    private DespesaResponse despesaMock;
    private RelatorioFinanceiroResponse relatorioMock;

    @BeforeEach
    void setUp() {
        despesaMock = new DespesaResponse(1L, 1L, "Energia Elétrica",
                "Conta de março", new BigDecimal("280.50"),
                LocalDate.now(), "BOLETO", null, "Admin", LocalDateTime.now());

        relatorioMock = new RelatorioFinanceiroResponse(
                LocalDate.now(), LocalDate.now(),
                new BigDecimal("3000.00"), 50L,
                new BigDecimal("800.00"), List.of(),
                new BigDecimal("2200.00"), new BigDecimal("73.33"),
                new BigDecimal("150.00"), 3L, List.of()
        );
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /financeiro/despesas deve lançar despesa com status 201")
    void lancarDespesa_dadosValidos_retorna201() throws Exception {
        when(financeiroService.lancarDespesa(any())).thenReturn(despesaMock);
        var req = new DespesaRequest(1L, "Conta de março", new BigDecimal("280.50"),
                LocalDate.now(), Despesa.FormaPagamentoDespesa.BOLETO, null);

        mockMvc.perform(post("/api/v1/financeiro/despesas")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Conta de março"))
                .andExpect(jsonPath("$.valor").value(280.50))
                .andExpect(jsonPath("$.tipoDespesaNome").value("Energia Elétrica"));
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("POST /financeiro/despesas com valor zero deve retornar 400")
    void lancarDespesa_valorZero_retorna400() throws Exception {
        var req = new DespesaRequest(1L, "Teste", BigDecimal.ZERO, LocalDate.now(), null, null);

        mockMvc.perform(post("/api/v1/financeiro/despesas")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.valor").exists());
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /financeiro/relatorio/hoje deve retornar relatório completo")
    void relatorioHoje_retornaRelatorio() throws Exception {
        when(financeiroService.gerarRelatorioHoje()).thenReturn(relatorioMock);

        mockMvc.perform(get("/api/v1/financeiro/relatorio/hoje"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVendas").value(3000.00))
                .andExpect(jsonPath("$.totalDespesas").value(800.00))
                .andExpect(jsonPath("$.lucroLiquido").value(2200.00))
                .andExpect(jsonPath("$.totalFiadoEmAberto").value(150.00));
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /financeiro/saldo-hoje deve retornar saldo do dia")
    void getSaldoHoje_retornaSaldo() throws Exception {
        var saldo = new FinanceiroService.SaldoCaixaDiaResponse(
                LocalDate.now(), new BigDecimal("1200.00"),
                new BigDecimal("300.00"), new BigDecimal("900.00"), "POSITIVO");
        when(financeiroService.getSaldoHoje()).thenReturn(saldo);

        mockMvc.perform(get("/api/v1/financeiro/saldo-hoje"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(900.00))
                .andExpect(jsonPath("$.situacao").value("POSITIVO"));
    }


    @Test @WithMockUser(roles = "GERENTE")
    @DisplayName("DELETE /financeiro/despesas/{id} por GERENTE deve retornar 204")
    void excluirDespesa_comPermissao_retorna204() throws Exception {
        doNothing().when(financeiroService).excluirDespesa(1L);

        mockMvc.perform(delete("/api/v1/financeiro/despesas/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test @WithMockUser(roles = "OPERADOR")
    @DisplayName("GET /financeiro/despesas/{id} inexistente deve retornar 404")
    void buscarDespesa_inexistente_retorna404() throws Exception {
        when(financeiroService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Despesa", 99L));

        mockMvc.perform(get("/api/v1/financeiro/despesas/99"))
                .andExpect(status().isNotFound());
    }
}
