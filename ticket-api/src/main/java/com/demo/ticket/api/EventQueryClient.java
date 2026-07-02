package com.demo.ticket.api;

import com.demo.ticket.constants.TenantConstants;
import com.demo.ticket.dto.EventResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 活动查询 Feign 客户端 — order-service 获取活动详情。
 * <p>
 * 租户 Header 显式传参，兼容 CircuitBreaker 异步线程（ThreadLocal 不可用）。
 * </p>
 */
@FeignClient(name = "event-service", contextId = "eventQueryClient")
public interface EventQueryClient {

    @GetMapping("/internal/events/{eventId}")
    EventResponse getEvent(
            @PathVariable("eventId") Long eventId,
            @RequestHeader(TenantConstants.TENANT_HEADER) String tenantId,
            @RequestHeader(value = TenantConstants.TENANT_TIER_HEADER, required = false) String tier,
            @RequestHeader(value = TenantConstants.TENANT_PLUGINS_HEADER, required = false) String plugins);
}
