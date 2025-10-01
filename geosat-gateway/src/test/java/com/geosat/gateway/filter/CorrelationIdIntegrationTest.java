package com.geosat.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para verificar que o CorrelationIdFilter está funcionando
 * corretamente em requisições reais.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorrelationIdIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveAdicionarCorrelationIdNoHeaderDeResposta() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/estacoes"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andReturn();

        String correlationId = result.getResponse().getHeader("X-Correlation-ID");
        assertThat(correlationId).isNotNull().isNotEmpty();
        assertThat(correlationId).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    void devePreservarCorrelationIdEnviadoPeloCliente() throws Exception {
        String clientCorrelationId = "client-test-id-12345";
        
        MvcResult result = mockMvc.perform(get("/api/v1/estacoes")
                        .header("X-Correlation-ID", clientCorrelationId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-ID", clientCorrelationId))
                .andReturn();

        String responseCorrelationId = result.getResponse().getHeader("X-Correlation-ID");
        assertThat(responseCorrelationId).isEqualTo(clientCorrelationId);
    }

    @Test
    void deveGerarDiferentesCorrelationIdsParaRequisicoesDiferentes() throws Exception {
        MvcResult result1 = mockMvc.perform(get("/api/v1/estacoes"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result2 = mockMvc.perform(get("/api/v1/estacoes"))
                .andExpect(status().isOk())
                .andReturn();

        String correlationId1 = result1.getResponse().getHeader("X-Correlation-ID");
        String correlationId2 = result2.getResponse().getHeader("X-Correlation-ID");

        assertThat(correlationId1).isNotEqualTo(correlationId2);
    }
}
