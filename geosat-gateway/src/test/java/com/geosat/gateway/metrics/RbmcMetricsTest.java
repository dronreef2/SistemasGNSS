package com.geosat.gateway.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RbmcMetricsTest {

    private RbmcMetrics metrics;
    private MeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new RbmcMetrics(registry);
    }

    @Test
    void deveRegistrarCacheHit() {
        // Act
        metrics.recordCacheHit();

        // Assert
        double count = registry.counter("rbmc.cache.hits").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void deveRegistrarCacheMiss() {
        // Act
        metrics.recordCacheMiss();

        // Assert
        double count = registry.counter("rbmc.cache.misses").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void deveCalcularCacheHitRate() {
        // Arrange
        metrics.recordCacheHit();
        metrics.recordCacheHit();
        metrics.recordCacheHit();
        metrics.recordCacheMiss();

        // Act
        double hitRate = metrics.getCacheHitRate();

        // Assert
        assertThat(hitRate).isEqualTo(75.0);
    }

    @Test
    void deveRetornarZeroSeNaoHouverDados() {
        // Act
        double hitRate = metrics.getCacheHitRate();

        // Assert
        assertThat(hitRate).isEqualTo(0.0);
    }

    @Test
    void deveCriarTimerPorEstacao() {
        // Act
        Timer timer1 = metrics.getStationLatencyTimer("ALAR");
        Timer timer2 = metrics.getStationLatencyTimer("BOMJ");
        Timer timer3 = metrics.getStationLatencyTimer("ALAR"); // Reusar

        // Assert
        assertThat(timer1).isNotNull();
        assertThat(timer2).isNotNull();
        assertThat(timer1).isSameAs(timer3); // Mesmo timer para mesma estação
        assertThat(timer1).isNotSameAs(timer2); // Timers diferentes para estações diferentes
    }

    @Test
    void deveRegistrarLatenciaPorEstacao() {
        // Arrange
        Timer timer = metrics.getStationLatencyTimer("ALAR");

        // Act
        timer.record(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Assert
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThan(0);
    }

    @Test
    void deveNormalizarEstacaoParaMaiuscula() {
        // Act
        Timer timer1 = metrics.getStationLatencyTimer("alar");
        Timer timer2 = metrics.getStationLatencyTimer("ALAR");

        // Assert
        assertThat(timer1).isSameAs(timer2);
    }
}
