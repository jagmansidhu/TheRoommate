package com.roomate.app.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Distributed rate limiting filter backed by Redis via Bucket4j.
 *
 * Limits (per IP, shared across all pods via Redis):
 *   - Auth endpoints (/user/login, /user/register, /user/verify): 5 req/min
 *   - General endpoints: 100 req/min
 *
 * Graceful degradation: if Redis is unavailable the request is allowed through
 * and a warning is logged, so a Redis outage doesn't take down the API.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final ProxyManager<String> proxyManager;

    // Bucket configurations — defined once, reused per-key in Redis
    private final Supplier<BucketConfiguration> authBucketConfig;
    private final Supplier<BucketConfiguration> generalBucketConfig;

    public RateLimitingFilter(
            @Value("${spring.data.redis.url:redis://localhost:6379}") String redisUrl) {

        // Build a Lettuce RedisClient from the configured URL
        RedisClient redisClient = RedisClient.create(redisUrl);
        StatefulRedisConnection<String, byte[]> connection =
                redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        this.proxyManager = LettuceBasedProxyManager.builderFor(connection)
                .build();

        // Auth: 5 requests per minute per IP
        this.authBucketConfig = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build();

        // General: 100 requests per minute per IP
        this.generalBucketConfig = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String clientIp = getClientIP(request);
        String path = request.getRequestURI();

        // Namespace the Redis key by endpoint type to keep auth and general limits separate
        boolean isAuth = isAuthEndpoint(path);
        String bucketKey = (isAuth ? "rate:auth:" : "rate:general:") + clientIp;
        Supplier<BucketConfiguration> configSupplier = isAuth ? authBucketConfig : generalBucketConfig;

        try {
            boolean allowed = proxyManager
                    .builder()
                    .build(bucketKey, configSupplier)
                    .tryConsume(1);

            if (allowed) {
                chain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
            }
        } catch (Exception e) {
            // Redis unavailable — fail open so an outage doesn't block the API
            log.warn("Redis rate limiter unavailable for IP {}, allowing request through: {}", clientIp, e.getMessage());
            chain.doFilter(request, response);
        }
    }

    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/user/login") ||
                path.startsWith("/user/register") ||
                path.startsWith("/user/verify");
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }
}
