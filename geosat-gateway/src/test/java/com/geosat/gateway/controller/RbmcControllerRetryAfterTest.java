package com.geosat.gateway.controller;

import com.geosat.gateway.model.RbmcFallbackResponse;
import com.geosat.gateway.service.CircuitBreakerStateService;
import com.geosat.gateway.service.RbmcService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Map;

@WebMvcTest(RbmcController.class)
class RbmcControllerRetryAfterTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RbmcService service;

    @MockBean
    CircuitBreakerStateService cbState;

    @BeforeEach
    void setup(){
    // Simulamos 12 segundos restantes; serviço agora calcula dinamicamente
    Mockito.when(cbState.remainingOpenSeconds()).thenReturn(java.util.Optional.of(12L));
    }

    @Test
    void fallbackIncludesRetryAfter() throws Exception {
        Mockito.when(service.obterRelatorio(anyString())).thenReturn(
                new RbmcFallbackResponse("ALAR","indisponivel","CB aberto", Instant.now(), Map.of())
        );
        mockMvc.perform(get("/api/v1/rbmc/ALAR/relatorio"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "12"));
    }
}
