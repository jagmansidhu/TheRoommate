package com.roomate.app.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Rate limiting filter — distributed via Redis when available, fail-open otherwise.
 *
 * Limits (per IP, shared across all pods):
 *   - Auth endpoints (/user/login, /user/register, /user/verify): 5 req/min
 *   - General endpoints: 100 req/min
 *
 * If the Redis ProxyManager is null (Redis unreachable at startup) all requests are allowed
 * through and a warning is logged so the API stays up.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Injected as optional — null when Redis was unavailable at startup
    private final ProxyManager<String> proxyManager;

    private final Supplier<BucketConfiguration> authBucketConfig;
    private final Supplier<BucketConfiguration> generalBucketConfig;

    // @Autowired(required = false) so Spring doesn't fail if rateLimitProxyManager bean is null
    public RateLimitingFilter(@Autowired(required = false) @Nullable ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;

        // Auth: 5 requests per minute per IP
        this.authBucketConfig = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillIntervally(5, Duration.ofMinutes(1))
                        .build())
                .build();

        // General: 100 requests per minute per IP
        this.generalBucketConfig = () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(100)
                        .refillIntervally(100, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Fail-open: if Redis wasn't available at startup, skip rate limiting
        if (proxyManager == null) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIP(request);
        String path = request.getRequestURI();

        boolean isAuth = isAuthEndpoint(path);
        // Namespace Redis keys by type to keep auth and general counters separate
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
            // Redis error mid-request — fail open
            log.warn("Rate limiter error for IP {}, allowing request: {}", clientIp, e.getMessage());
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
