package com.geosat.gateway;

import com.geosat.gateway.client.RbmcHttpClient;
import com.geosat.gateway.service.RedisCacheService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@AutoConfigureMockMvc
class RbmcAdditionalEndpointsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RbmcHttpClient rbmcHttpClient;

    @MockBean
    RedisCacheService redisCacheService;

    @Test
    void deveRetornarRinex2Metadata() throws Exception {
        Mockito.when(redisCacheService.getMetadata(Mockito.anyString())).thenReturn(java.util.Optional.empty());
        Mockito.when(rbmcHttpClient.obterArquivo(Mockito.contains("rinex2"))).thenReturn("ok");
        mockMvc.perform(get("/api/v1/rbmc/rinex2/ALAR/2024/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("rinex2"))
                .andExpect(jsonPath("$.intervalo").value("15s"));
    }

    @Test
    void deveRetornarFallbackRinex3QuandoFalha() throws Exception {
    Mockito.when(redisCacheService.getMetadata(Mockito.anyString())).thenReturn(java.util.Optional.empty());
        Mockito.when(rbmcHttpClient.obterArquivo(Mockito.contains("rinex3/1s")))
                .thenThrow(new RuntimeException("falha"));
        mockMvc.perform(get("/api/v1/rbmc/rinex3/1s/ALAR/2024/12/0/15/MO"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("indisponivel"));
    }

    @Test
    void deveRetornarOrbitas() throws Exception {
        Mockito.when(redisCacheService.getMetadata(Mockito.anyString())).thenReturn(java.util.Optional.empty());
        Mockito.when(rbmcHttpClient.obterArquivo(Mockito.contains("orbitas"))).thenReturn("ok");
        mockMvc.perform(get("/api/v1/rbmc/rinex3/orbitas/2024/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("orbitas"));
    }
}
