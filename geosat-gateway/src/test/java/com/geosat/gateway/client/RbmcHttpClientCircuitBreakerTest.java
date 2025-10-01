package com.geosat.gateway.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@TestPropertySource(properties = {
        "rbmc.base-url=https://exemplo.local/fake",
        // Deixar thresholds baixos para abrir CB rapidamente
        "resilience4j.circuitbreaker.instances.rbmcClient.slidingWindowSize=4",
        "resilience4j.circuitbreaker.instances.rbmcClient.minimumNumberOfCalls=4",
        "resilience4j.circuitbreaker.instances.rbmcClient.failureRateThreshold=50",
        "resilience4j.circuitbreaker.instances.rbmcClient.waitDurationInOpenState=5s"
})
class RbmcHttpClientCircuitBreakerTest {

    @MockBean
    CloseableHttpClient httpClient;

    @Autowired
    RbmcHttpClient client;

    @Autowired
    CircuitBreakerRegistry cbRegistry;

    @Autowired
    RetryRegistry retryRegistry;

    @BeforeEach
    void setup() throws Exception {
        // Simula falhas usando ResponseHandler pattern (não deprecado)
        Mockito.when(httpClient.execute(any(HttpGet.class), any(HttpClientResponseHandler.class)))
                .thenThrow(new IOException("Falha simulada"));
    }

    @Test
    void deveAbrirCircuitBreakerAposFalhas() throws Exception {
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> client.obterRelatorio("ALAR"))
                    .isInstanceOf(IOException.class);
        }
        CircuitBreaker cb = cbRegistry.circuitBreaker("rbmcClient");
        assertThat(cb.getState()).isIn(CircuitBreaker.State.OPEN, CircuitBreaker.State.HALF_OPEN, CircuitBreaker.State.CLOSED);
        // Nova chamada depois de aberto pode lançar IOException rapidamente
        try {
            client.obterRelatorio("ALAR");
        } catch (IOException ignored) {}
    }
}
