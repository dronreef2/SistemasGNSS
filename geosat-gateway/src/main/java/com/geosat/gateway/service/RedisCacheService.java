package com.geosat.gateway.service;

import com.geosat.gateway.metrics.RbmcMetrics;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, String, Object> hashOps;
    private final RbmcMetrics metrics;
    private static final String PREFIX = "rbmc:meta:";

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate, RbmcMetrics metrics) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
        this.metrics = metrics;
    }

    public void putMetadata(String estacao, Map<String,Object> data, Duration ttl) {
        String key = key(estacao);
        hashOps.putAll(key, data);
        redisTemplate.expire(key, ttl);
    }

    public Optional<Map<String,Object>> getMetadata(String estacao) {
        String key = key(estacao);
        Map<String,Object> all = hashOps.entries(key);
        if (all == null || all.isEmpty()) {
            metrics.recordCacheMiss();
            return Optional.empty();
        }
        metrics.recordCacheHit();
        return Optional.of(all);
    }

    private String key(String estacao) {
        return PREFIX + estacao.toUpperCase();
    }
}
