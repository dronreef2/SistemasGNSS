package com.geosat.gateway;

import com.geosat.gateway.client.RbmcHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class RedisFallbackIntegrationTest {

    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        if (!redis.isRunning()) {
            redis.start();
        }
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RbmcHttpClient rbmcHttpClient;


    @BeforeEach
    void setup() throws Exception {
    // Simula primeira resposta ok e depois falha
    Mockito.when(rbmcHttpClient.obterRelatorio("CACH"))
        .thenReturn("ok")
        .thenThrow(new RuntimeException("Erro simulado"));

    mockMvc.perform(get("/api/v1/rbmc/CACH/relatorio"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estacao").value("CACH"));
    }

    @Test
    void deveUsarCacheNoFallback() throws Exception {
        // Segunda chamada aciona fallback e deve trazer dadosCacheados
    mockMvc.perform(get("/api/v1/rbmc/CACH/relatorio"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.status").value("indisponivel"))
        .andExpect(jsonPath("$.dadosCacheados.link").value("https://servicodados.ibge.gov.br/api/v1/rbmc/relatorio/cach"));
    }
}
