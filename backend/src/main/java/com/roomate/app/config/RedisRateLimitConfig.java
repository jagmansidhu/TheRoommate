package com.roomate.app.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates the Redis-backed Bucket4j ProxyManager used for distributed rate limiting.
 *
 * Registered as a separate @Bean so it is created lazily by the Spring context rather than
 * eagerly inside the filter constructor. If Redis is unavailable at startup the bean returns
 * null and the filter falls back to allow-all (fail-open), preventing a Redis outage from
 * crashing the backend on boot.
 */
@Configuration
public class RedisRateLimitConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitConfig.class);

    /**
     * Returns a Redis-backed ProxyManager, or null when Redis is unreachable.
     * The filter checks for null and degrades gracefully.
     */
    @Bean
    public ProxyManager<String> rateLimitProxyManager(
            @Value("${spring.data.redis.url:redis://localhost:6379}") String redisUrl) {
        try {
            RedisClient client = RedisClient.create(redisUrl);
            StatefulRedisConnection<String, byte[]> connection =
                    client.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
            log.info("Redis rate limiting connected to {}", redisUrl);
            return LettuceBasedProxyManager.builderFor(connection).build();
        } catch (Exception e) {
            // Redis unavailable — return null so the filter degrades to fail-open.
            log.warn("Redis unavailable for rate limiting ({}), falling back to allow-all: {}",
                    redisUrl, e.getMessage());
            return null;
        }
    }
}
