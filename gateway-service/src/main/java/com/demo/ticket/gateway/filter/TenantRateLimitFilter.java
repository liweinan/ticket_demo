package com.demo.ticket.gateway.filter;

import com.demo.ticket.constants.TenantConstants;
import com.demo.ticket.gateway.ratelimit.TenantRateLimitProperties;
import com.demo.ticket.tenant.TenantTier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gateway 层 Resilience4j 租户限流 — 替换单体应用内存滑动窗口。
 */
public class TenantRateLimitFilter implements GatewayFilter {

    private final TenantRateLimitProperties properties;
    private final Map<String, RateLimiter> limiterCache = new ConcurrentHashMap<>();

    public TenantRateLimitFilter(TenantRateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/api/tenants") || path.startsWith("/api/health")) {
            return chain.filter(exchange);
        }

        String tenantId = exchange.getRequest().getHeaders().getFirst(TenantConstants.TENANT_HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            return chain.filter(exchange);
        }

        Integer tenantMaxQps = exchange.getAttribute("tenantMaxQps");
        TenantTier tier = exchange.getAttribute("tenantTier");
        int maxQps = properties.resolveMaxQps(tier, tenantMaxQps != null ? tenantMaxQps : 0);

        RateLimiter limiter = limiterCache.compute(tenantId, (key, existing) -> {
            if (existing != null) {
                return existing;
            }
            RateLimiterConfig config = RateLimiterConfig.custom()
                    .limitForPeriod(maxQps)
                    .limitRefreshPeriod(Duration.ofSeconds(1))
                    .timeoutDuration(Duration.ZERO)
                    .build();
            return RateLimiterRegistry.of(config).rateLimiter(key);
        });

        try {
            if (!limiter.acquirePermission()) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "租户 " + tenantId + " 超出 QPS 限制 (" + maxQps + "/s)，请稍后重试"));
            }
        } catch (RequestNotPermitted ex) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "租户 " + tenantId + " 超出 QPS 限制 (" + maxQps + "/s)，请稍后重试"));
        }

        return chain.filter(exchange);
    }
}
