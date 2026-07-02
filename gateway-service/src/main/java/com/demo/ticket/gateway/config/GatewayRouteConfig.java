package com.demo.ticket.gateway.config;

import com.demo.ticket.gateway.filter.TenantRateLimitFilter;
import com.demo.ticket.gateway.filter.TenantValidationFilter;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Gateway 路由与 WebClient 配置 — 基于 Nacos 服务发现负载均衡。
 */
@Configuration
public class GatewayRouteConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public RouteLocator customRoutes(
            RouteLocatorBuilder builder,
            TenantValidationFilter tenantValidationFilter,
            TenantRateLimitFilter tenantRateLimitFilter) {
        return builder.routes()
                .route("tenant-service", r -> r
                        .path("/api/tenants/**")
                        .filters(f -> f.filter(tenantValidationFilter)
                                .filter(tenantRateLimitFilter))
                        .uri("lb://tenant-service"))
                .route("event-service", r -> r
                        .path("/api/events/**")
                        .filters(f -> f.filter(tenantValidationFilter)
                                .filter(tenantRateLimitFilter))
                        .uri("lb://event-service"))
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.filter(tenantValidationFilter)
                                .filter(tenantRateLimitFilter))
                        .uri("lb://order-service"))
                .route("health-tenant", r -> r
                        .path("/api/health/tenant")
                        .uri("lb://tenant-service/api/health"))
                .route("health-event", r -> r
                        .path("/api/health/event")
                        .uri("lb://event-service/api/health"))
                .route("health-order", r -> r
                        .path("/api/health/order")
                        .uri("lb://order-service/api/health"))
                .build();
    }

    @Bean
    public TenantValidationFilter tenantValidationFilter(WebClient.Builder webClientBuilder) {
        WebClient webClient = webClientBuilder.build();
        return new TenantValidationFilter(webClient);
    }

    @Bean
    public TenantRateLimitFilter tenantRateLimitFilter(
            com.demo.ticket.gateway.ratelimit.TenantRateLimitProperties properties) {
        return new TenantRateLimitFilter(properties);
    }
}
