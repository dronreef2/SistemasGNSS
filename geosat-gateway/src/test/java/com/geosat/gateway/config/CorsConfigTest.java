package com.geosat.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devePermitirOrigemConfigurada() throws Exception {
        mockMvc.perform(get("/api/v1/rbmc/ALAR/relatorio")
                .header("Origin", "http://localhost:8080"))
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void deveExporCorrelationIdHeader() throws Exception {
        mockMvc.perform(options("/api/v1/rbmc/ALAR/relatorio")
                .header("Origin", "http://localhost:8080")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Expose-Headers", 
                    org.hamcrest.Matchers.containsString("X-Correlation-ID")));
    }

    @Test
    void deveExportRetryAfterHeader() throws Exception {
        mockMvc.perform(options("/api/v1/rbmc/ALAR/relatorio")
                .header("Origin", "http://localhost:8080")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Expose-Headers", 
                    org.hamcrest.Matchers.containsString("Retry-After")));
    }

    @Test
    void devePermitirMetodosConfigurados() throws Exception {
        mockMvc.perform(options("/api/v1/rbmc/ALAR/relatorio")
                .header("Origin", "http://localhost:8080")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    void deveConfigurarMaxAge() throws Exception {
        mockMvc.perform(options("/api/v1/rbmc/ALAR/relatorio")
                .header("Origin", "http://localhost:8080")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }
}
