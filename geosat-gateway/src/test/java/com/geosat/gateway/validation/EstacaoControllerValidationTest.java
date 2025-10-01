package com.geosat.gateway.validation;

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
    void deveValidarCodigoEstacaoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ABC/snr")
                .param("ano", "2023")
                .param("dia", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveValidarAnoMinimoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                .param("ano", "1999")
                .param("dia", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveValidarAnoMaximoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                .param("ano", "2101")
                .param("dia", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveValidarDiaMinimoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                .param("ano", "2023")
                .param("dia", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveValidarDiaMaximoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                .param("ano", "2023")
                .param("dia", "367"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveValidarMaxInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                .param("ano", "2023")
                .param("dia", "100")
                .param("max", "10001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveAceitarParametrosValidos() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr")
                .param("ano", "2023")
                .param("dia", "100")
                .param("max", "300"))
                .andExpect(status().isOk());
    }

    @Test
    void deveValidarCodigoEstacaoNoEndpointPosicoes() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/AB/posicoes")
                .param("ano", "2023")
                .param("dia", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveValidarCodigoEstacaoNoEndpointMetadados() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/AB123/metadados"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
