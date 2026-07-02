package com.demo.ticket.api;

import com.demo.ticket.dto.TenantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 租户服务 Feign 客户端 — 供 Gateway 校验租户、Order 服务加载插件配置。
 */
@FeignClient(name = "tenant-service")
public interface TenantClient {

    @GetMapping("/internal/tenants/{tenantId}")
    TenantResponse getTenant(@PathVariable("tenantId") String tenantId);
}
