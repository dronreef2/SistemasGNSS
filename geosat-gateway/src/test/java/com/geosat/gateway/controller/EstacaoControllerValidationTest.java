package com.geosat.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EstacaoControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornar400QuandoCodigoEstacaoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/AL1/snr")
                        .param("ano", "2024")
                        .param("dia", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void deveRetornar400QuandoAnoForaDoRange() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                        .param("ano", "1999")
                        .param("dia", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void deveRetornar400QuandoDiaForaDoRange() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                        .param("ano", "2024")
                        .param("dia", "367"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void deveRetornar400QuandoDiaNegativo() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                        .param("ano", "2024")
                        .param("dia", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void deveRetornar400QuandoMaxMuitoGrande() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                        .param("ano", "2024")
                        .param("dia", "100")
                        .param("max", "20000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void deveAceitarParametrosValidos() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                        .param("ano", "2024")
                        .param("dia", "100")
                        .param("max", "300"))
                .andExpect(status().isOk());
    }

    @Test
    void deveValidarEndpointPosicoes() throws Exception {
        // Teste com ano inválido
        mockMvc.perform(get("/api/v1/estacoes/ALAR/posicoes")
                        .param("ano", "2200")
                        .param("dia", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void deveAceitarParametrosValidosEmPosicoes() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/posicoes")
                        .param("ano", "2024")
                        .param("dia", "100")
                        .param("max", "500"))
                .andExpect(status().isOk());
    }
}
