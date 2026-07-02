package com.demo.ticket.gateway.filter;

import com.demo.ticket.constants.TenantConstants;
import com.demo.ticket.dto.TenantResponse;
import com.demo.ticket.tenant.TenantTier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * 租户校验过滤器 — 调用 tenant-service 验证租户并透传元数据 Header。
 * <p>
 * {@code /api/tenants} 列表接口不要求 X-Tenant-ID。
 * </p>
 */
public class TenantValidationFilter implements GatewayFilter {

    private static final Set<String> TENANT_OPTIONAL_PATHS = Set.of("/api/tenants", "/api/health");

    private final WebClient webClient;

    public TenantValidationFilter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (isTenantOptional(path)) {
            return chain.filter(exchange);
        }

        String tenantId = exchange.getRequest().getHeaders().getFirst(TenantConstants.TENANT_HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "缺少请求头: " + TenantConstants.TENANT_HEADER));
        }

        return webClient.get()
                .uri("http://tenant-service/internal/tenants/{tenantId}", tenantId.trim())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TenantResponse.class)
                .flatMap(tenant -> {
                    ServerWebExchange mutated = exchange.mutate()
                            .request(builder -> builder.headers(headers -> {
                                headers.set(TenantConstants.TENANT_HEADER, tenant.tenantId());
                                headers.set(TenantConstants.TENANT_TIER_HEADER, tierName(tenant.tier()));
                                headers.set(TenantConstants.TENANT_PLUGINS_HEADER,
                                        tenant.enabledPlugins() != null ? tenant.enabledPlugins() : "");
                                exchange.getAttributes().put("tenantMaxQps", tenant.maxQps());
                                exchange.getAttributes().put("tenantTier", tenant.tier());
                            }))
                            .build();
                    return chain.filter(mutated);
                })
                .onErrorMap(ex -> !(ex instanceof ResponseStatusException), ex ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "未知租户: " + tenantId));
    }

    private boolean isTenantOptional(String path) {
        return TENANT_OPTIONAL_PATHS.stream().anyMatch(path::startsWith);
    }

    private String tierName(TenantTier tier) {
        return tier != null ? tier.name() : TenantTier.BRONZE.name();
    }
}
