package com.chenliang.chat.aiservice.state;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Spring Data Redis 的具体实现。
 * 解决了 Session 依赖单台机器的问题，适应微服务集群架构。
 */
@Service
public class RedisStateStore implements StateStore {

    private static final String REDIS_PREFIX = "chat:state:";
    private final StringRedisTemplate redisTemplate;

    public RedisStateStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(String userId, String stateKey) {
        return REDIS_PREFIX + userId + ":" + stateKey;
    }

    @Override
    public void set(String userId, String key, Object value) {
        if (userId == null || key == null || value == null) return;
        String fullKey = java.util.Objects.requireNonNull(buildKey(userId, key));
        // 使用 String 格式存储，有效期默认 24 小时
        redisTemplate.opsForValue().set(fullKey, value.toString(), 24, TimeUnit.HOURS);
    }

    @Override
    public Object get(String userId, String key) {
        if (userId == null || key == null) return null;
        return redisTemplate.opsForValue().get(java.util.Objects.requireNonNull(buildKey(userId, key)));
    }

    @Override
    public void remove(String userId, String key) {
        if (userId == null || key == null) return;
        redisTemplate.delete(java.util.Objects.requireNonNull(buildKey(userId, key)));
    }
}
