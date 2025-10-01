package com.geosat.gateway.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@TestPropertySource(properties = {
    "rbmc.base-url=https://exemplo.local/fake"
})
class RbmcHttpClientRetryTest {

    @MockBean
    CloseableHttpClient httpClient;

    @Autowired
    RbmcHttpClient client;

    @Autowired
    RetryRegistry retryRegistry;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setup() throws Exception {
        // Simula 3 falhas e depois sucesso não alcançado (porque max-attempts=4). Vamos sempre falhar.
        Mockito.when(httpClient.execute(any(HttpGet.class)))
                .then(invocation -> {
                    throw new IOException("Falha simulada");
                });
    }

    @Test
    void deveAplicarRetryAteLimite() throws Exception {
        assertThatThrownBy(() -> client.obterRelatorio("ALAR"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Falha simulada");

    // Espera 4 tentativas (max-attempts=4)
    Mockito.verify(httpClient, times(4)).execute(any(HttpGet.class), any());
    }
}
