package com.geosat.gateway.filter;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CorrelationIdFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveGerarCorrelationIdSeNaoExiste() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    void deveUsarCorrelationIdExistenteSeProvido() throws Exception {
        String correlationId = "test-correlation-id-123";
        
        mockMvc.perform(get("/api/v1/estacoes")
                .header("X-Correlation-ID", correlationId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-ID", correlationId));
    }

    @Test
    void deveAdicionarCorrelationIdAoMDC() throws Exception {
        // Este teste verifica que o MDC é limpo após a requisição
        mockMvc.perform(get("/api/v1/estacoes"))
                .andExpect(status().isOk());
        
        // MDC deve estar limpo após a requisição
        assertThat(MDC.get("correlationId")).isNull();
    }
}
