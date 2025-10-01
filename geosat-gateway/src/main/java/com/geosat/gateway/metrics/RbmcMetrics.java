package com.geosat.gateway.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Métricas customizadas para o sistema RBMC.
 * Rastreia cache hits/misses e latência por estação.
 */
@Component
public class RbmcMetrics {
    
    private final MeterRegistry registry;
    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final ConcurrentHashMap<String, Timer> latencyByStation;
    
    public RbmcMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.cacheHits = Counter.builder("rbmc.cache.hits")
                .description("Total number of cache hits")
                .register(registry);
        this.cacheMisses = Counter.builder("rbmc.cache.misses")
                .description("Total number of cache misses")
                .register(registry);
        this.latencyByStation = new ConcurrentHashMap<>();
    }
    
    /**
     * Registra um cache hit
     */
    public void recordCacheHit() {
        cacheHits.increment();
    }
    
    /**
     * Registra um cache miss
     */
    public void recordCacheMiss() {
        cacheMisses.increment();
    }
    
    /**
     * Calcula a taxa de cache hit em percentual
     * @return Taxa de hit (0-100) ou 0 se não houver dados
     */
    public double getCacheHitRate() {
        double hits = cacheHits.count();
        double misses = cacheMisses.count();
        double total = hits + misses;
        return total > 0 ? (hits / total) * 100 : 0;
    }
    
    /**
     * Obtém o timer de latência para uma estação específica.
     * Cria o timer se não existir.
     * @param estacao Código da estação
     * @return Timer para medir latência
     */
    public Timer getStationLatencyTimer(String estacao) {
        return latencyByStation.computeIfAbsent(estacao, 
            e -> Timer.builder("rbmc.requests.latency.by_station")
                    .tag("estacao", e.toUpperCase())
                    .description("Request latency by station")
                    .register(registry)
        );
    }
}
