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
    void geojsonOk() throws Exception {
        mockMvc.perform(get("/api/v1/estacoes/geojson"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$.features[0].type").value("Feature"))
                .andExpect(jsonPath("$.features[0].geometry.type").value("Point"))
                .andExpect(jsonPath("$.features[0].geometry.coordinates").isArray())
                .andExpect(jsonPath("$.features[0].properties.codigo").exists());
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
