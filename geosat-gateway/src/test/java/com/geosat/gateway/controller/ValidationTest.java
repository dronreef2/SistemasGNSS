package com.geosat.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de validação de parâmetros de entrada nos endpoints REST.
 * Verifica se as anotações @Valid e validações customizadas estão funcionando corretamente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ValidationTest {

    @Autowired
    private MockMvc mockMvc;

    // Testes para EstacaoController

    @Test
    void deveRetornar400ParaEstacaoInvalida() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/AB/metadados"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void deveRetornar400ParaAnoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                        .param("ano", "1999")
                        .param("dia", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveRetornar400ParaDiaInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                        .param("ano", "2025")
                        .param("dia", "367"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveRetornar400ParaMaxInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                        .param("ano", "2025")
                        .param("dia", "100")
                        .param("max", "20000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveRetornar200ParaParametrosValidos() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                        .param("ano", "2025")
                        .param("dia", "100")
                        .param("max", "300"))
                .andExpect(status().isOk());
    }

    // Testes para RbmcController
    // Nota: Estes testes validam parâmetros no path, não precisam de Redis

    @Test
    void deveRetornar400ParaEstacaoInvalidaRbmc() throws Exception {
        mockMvc.perform(get("/api/v1/rbmc/A/relatorio"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveRetornar400ParaAnoInvalidoRbmc() throws Exception {
        mockMvc.perform(get("/api/v1/rbmc/rinex2/ALAR/999/100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveRetornar400ParaDiaInvalidoRbmc() throws Exception {
        // Usando 1000 como dia inválido (maior que 366)
        mockMvc.perform(get("/api/v1/rbmc/rinex2/ALAR/2025/1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
