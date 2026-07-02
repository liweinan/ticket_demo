package com.demo.ticket.gateway.controller;

import com.demo.ticket.dto.HealthResponse;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 聚合健康检查 — 汇总各微服务与基础设施状态。
 */
@RestController
public class GatewayHealthController {

    private final WebClient webClient;
    private final DiscoveryClient discoveryClient;

    public GatewayHealthController(@LoadBalanced WebClient.Builder webClientBuilder,
                                   DiscoveryClient discoveryClient) {
        this.webClient = webClientBuilder.build();
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/api/health")
    public Mono<HealthResponse> health() {
        boolean nacosUp = discoveryClient.getServices().size() > 0;

        Mono<Boolean> tenantUp = probe("tenant-service");
        Mono<Boolean> eventUp = probe("event-service");
        Mono<Boolean> orderUp = probe("order-service");

        return Mono.zip(tenantUp, eventUp, orderUp)
                .map(tuple -> {
                    boolean postgresUp = tuple.getT1() && tuple.getT2() && tuple.getT3();
                    return HealthResponse.of(postgresUp, postgresUp, nacosUp, nacosUp);
                })
                .onErrorReturn(HealthResponse.of(false, false, nacosUp, false));
    }

    private Mono<Boolean> probe(String serviceName) {
        List<org.springframework.cloud.client.ServiceInstance> instances =
                discoveryClient.getInstances(serviceName);
        if (instances.isEmpty()) {
            return Mono.just(false);
        }
        return webClient.get()
                .uri("http://" + serviceName + "/api/health")
                .retrieve()
                .bodyToMono(HealthResponse.class)
                .map(r -> "UP".equals(r.status()) || "DEGRADED".equals(r.status()))
                .onErrorReturn(false);
    }
}
