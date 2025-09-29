package com.geosat.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.geosat.gateway.client.RbmcHttpClient;
import com.geosat.gateway.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@AutoConfigureMockMvc
class RbmcControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RbmcHttpClient rbmcHttpClient; // evita chamadas reais externas

    @MockBean
    private RedisCacheService redisCacheService; // evita conexões Redis reais

    @BeforeEach
    void setup() {
        // Evita NullPointer (service.fallback usa Optional); devolve vazio sempre
        Mockito.when(redisCacheService.getMetadata(Mockito.anyString()))
                .thenReturn(java.util.Optional.empty());
    }

    @Test
    void deveRetornarRelatorioPlaceholder() throws Exception {
        Mockito.when(rbmcHttpClient.obterRelatorio("ALAR")).thenReturn("conteudoPdfMock");
        mockMvc.perform(get("/api/v1/rbmc/ALAR/relatorio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estacao").value("ALAR"))
                .andExpect(jsonPath("$.tipo").value("pdf"));
    }

    @Test
    void deveValidarEstacaoInvalida() throws Exception {
        mockMvc.perform(get("/api/v1/rbmc/AL/relatorio"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deveRetornarFallbackQuandoFalha() throws Exception {
    Mockito.when(rbmcHttpClient.obterRelatorio("FALH"))
        .thenThrow(new RuntimeException("Falha sim"));
    mockMvc.perform(get("/api/v1/rbmc/FALH/relatorio"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.status").value("indisponivel"));
    }
}
