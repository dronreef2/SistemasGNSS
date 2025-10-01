package com.geosat.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RbmcControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornar400SeEstacaoInvalida() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/AL/snr")
                .param("ano", "2024")
                .param("dia", "100")
                .param("max", "300"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deveRetornar400SeAnoForaDoRange() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                .param("ano", "1999")
                .param("dia", "100")
                .param("max", "300"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados de entrada inválidos"));
    }

    @Test
    void deveRetornar400SeDiaInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                .param("ano", "2024")
                .param("dia", "400")
                .param("max", "300"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados de entrada inválidos"));
    }

    @Test
    void deveRetornar400SeMaxMuitoGrande() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                .param("ano", "2024")
                .param("dia", "100")
                .param("max", "20000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados de entrada inválidos"));
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
    void deveValidarPosicoes() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/posicoes")
                .param("ano", "1999")
                .param("dia", "100")
                .param("max", "300"))
                .andExpect(status().isBadRequest());
    }
}
