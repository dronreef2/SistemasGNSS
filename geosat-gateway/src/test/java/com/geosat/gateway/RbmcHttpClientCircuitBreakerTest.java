package com.geosat.gateway;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

class RbmcHttpClientCircuitBreakerTest {

    @Test
    void deveAbrirCircuitBreakerAposFalhas() {
        // Configuração de teste: janela pequena para disparar o circuito rapidamente
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slidingWindowSize(2)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(1)
            .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker cb = registry.circuitBreaker("rbmc-test-cb");

        // Supplier que sempre lança para simular falha do cliente HTTP
        Supplier<String> failingSupplier = () -> { throw new RuntimeException("simulated server error"); };

        // Decorar com o CircuitBreaker
        Supplier<String> decorated = CircuitBreaker.decorateSupplier(cb, failingSupplier);

        // As primeiras chamadas devem propagar exceção
        assertThrows(RuntimeException.class, decorated::get);
        assertThrows(RuntimeException.class, decorated::get);

        // Após as falhas, o circuito deve estar OPEN
        assertEquals(CircuitBreaker.State.OPEN, cb.getState(), "Circuit breaker should be OPEN after repeated failures");
    }
}
