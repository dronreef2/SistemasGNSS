package com.geosat.gateway.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RbmcMetrics {
    
    private final MeterRegistry registry;
    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final ConcurrentHashMap<String, Timer> latencyByStation;
    
    public RbmcMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.cacheHits = Counter.builder("rbmc.cache.hits")
                .description("Cache hits")
                .register(registry);
        this.cacheMisses = Counter.builder("rbmc.cache.misses")
                .description("Cache misses")
                .register(registry);
        this.latencyByStation = new ConcurrentHashMap<>();
    }
    
    public void recordCacheHit() {
        cacheHits.increment();
    }
    
    public void recordCacheMiss() {
        cacheMisses.increment();
    }
    
    public double getCacheHitRate() {
        double hits = cacheHits.count();
        double misses = cacheMisses.count();
        double total = hits + misses;
        return total > 0 ? (hits / total) * 100 : 0;
    }
    
    public Timer getStationLatencyTimer(String estacao) {
        return latencyByStation.computeIfAbsent(estacao, 
            e -> Timer.builder("rbmc.requests.latency.by_station")
                    .tag("estacao", e)
                    .description("Latency by station")
                    .register(registry)
        );
    }
}
