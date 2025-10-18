package com.geosat.gateway;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

class RbmcHttpClientRetryTest {

    @Test
    void deveAplicarRetryAteLimite() {
        // Configuração de teste: limitar tentativas para 3
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(java.time.Duration.ofMillis(10))
            .build();

        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("rbmc-test-retry");

        AtomicInteger attempts = new AtomicInteger(0);

        Supplier<String> failingSupplier = () -> {
            attempts.incrementAndGet();
            throw new RuntimeException("simulated transient failure");
        };

        Supplier<String> decorated = Retry.decorateSupplier(retry, failingSupplier);

        // A chamada final deve propagar exceção depois de esgotar as tentativas
        assertThrows(RuntimeException.class, decorated::get);

        // Verifica que o número de tentativas foi igual ao configurado
        assertEquals(3, attempts.get(), "Retry should have attempted the configured number of times");
    }
}
