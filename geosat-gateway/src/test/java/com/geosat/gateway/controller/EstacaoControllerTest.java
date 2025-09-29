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
class EstacaoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void listaEstacoesOk() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").exists());
    }

    @Test
    void snrSerieOk() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/snr?ano=2025&dia=200&max=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.samples").isArray());
    }

    @Test
    void posicoesSerieOk() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/ALAR/posicoes?ano=2025&dia=200&max=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.samples").isArray());
    }
}
