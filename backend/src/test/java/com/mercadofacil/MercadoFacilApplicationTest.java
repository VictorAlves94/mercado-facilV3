package com.mercadofacil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("MercadoFácil — Smoke Test de inicialização")
class MercadoFacilApplicationTest {

    @Test
    @DisplayName("Contexto Spring deve carregar sem erros")
    void contextLoads() {
        // Se chegar aqui, todos os beans estão configurados corretamente
    }
}
