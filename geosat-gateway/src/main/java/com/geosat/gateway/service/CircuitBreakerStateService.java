package com.geosat.gateway.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Optional;

@Service
public class CircuitBreakerStateService {

    private final CircuitBreaker circuitBreaker;
    private final long configuredWaitOpenSeconds;
    private final AtomicReference<Instant> openSince = new AtomicReference<>();
    private final Duration openDuration;

    public CircuitBreakerStateService(CircuitBreakerRegistry registry,
                                      org.springframework.core.env.Environment env) {
        this.circuitBreaker = registry.circuitBreaker("rbmcClient");
    this.configuredWaitOpenSeconds = env.getProperty("rbmc.circuitBreaker.waitOpenSeconds", Long.class, 30L);
    this.openDuration = Duration.ofSeconds(configuredWaitOpenSeconds);
        // Listener para capturar transições e registrar timestamp de abertura real
        this.circuitBreaker.getEventPublisher().onStateTransition(this::onTransition);
    }

    public Optional<Long> remainingOpenSeconds() {
        if (circuitBreaker.getState() != CircuitBreaker.State.OPEN) {
            return Optional.empty();
        }
        Instant since = openSince.get();
        if (since == null) {
            // Caso raro: estado OPEN mas não capturamos evento (fallback heurístico)
            return Optional.of(configuredWaitOpenSeconds);
        }
        Duration elapsed = Duration.between(since, Instant.now());
        Duration remaining = openDuration.minus(elapsed);
        long secs = remaining.isNegative() ? 0 : remaining.getSeconds();
        if (secs <= 0) {
            // Se já está para sair de OPEN, ainda retornamos 1 segundo mínimo para evitar zero (RFC permite inteiro >=1)
            return Optional.of(1L);
        }
        return Optional.of(secs);
    }

    public boolean isOpen(){
        return circuitBreaker.getState() == CircuitBreaker.State.OPEN;
    }

    private void onTransition(CircuitBreakerOnStateTransitionEvent event){
        switch (event.getStateTransition()) {
            case CLOSED_TO_OPEN, HALF_OPEN_TO_OPEN -> openSince.set(Instant.now());
            case OPEN_TO_HALF_OPEN, OPEN_TO_CLOSED -> openSince.set(null);
            default -> { /* ignore outros */ }
        }
    }
}
