package com.geosat.gateway.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, String, Object> hashOps;
    private static final String PREFIX = "rbmc:meta:";

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    public void putMetadata(String estacao, Map<String,Object> data, Duration ttl) {
        String key = key(estacao);
        hashOps.putAll(key, data);
        redisTemplate.expire(key, ttl);
    }

    public Optional<Map<String,Object>> getMetadata(String estacao) {
        String key = key(estacao);
        Map<String,Object> all = hashOps.entries(key);
        if (all == null || all.isEmpty()) return Optional.empty();
        return Optional.of(all);
    }

    private String key(String estacao) {
        return PREFIX + estacao.toUpperCase();
    }
}
