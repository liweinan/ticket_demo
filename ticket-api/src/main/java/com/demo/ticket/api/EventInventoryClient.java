package com.demo.ticket.api;

import com.demo.ticket.constants.TenantConstants;
import com.demo.ticket.dto.InventoryDeductRequest;
import com.demo.ticket.dto.InventoryRestoreRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 库存内部 API Feign 客户端 — order-service 调用 event-service 两阶段扣减。
 */
@FeignClient(name = "event-service", contextId = "eventInventoryClient")
public interface EventInventoryClient {

    @PostMapping("/internal/inventory/deduct")
    boolean deduct(
            @RequestBody InventoryDeductRequest request,
            @RequestHeader(TenantConstants.TENANT_HEADER) String tenantId,
            @RequestHeader(value = TenantConstants.TENANT_TIER_HEADER, required = false) String tier,
            @RequestHeader(value = TenantConstants.TENANT_PLUGINS_HEADER, required = false) String plugins);

    @PostMapping("/internal/inventory/restore")
    void restore(
            @RequestBody InventoryRestoreRequest request,
            @RequestHeader(TenantConstants.TENANT_HEADER) String tenantId,
            @RequestHeader(value = TenantConstants.TENANT_TIER_HEADER, required = false) String tier,
            @RequestHeader(value = TenantConstants.TENANT_PLUGINS_HEADER, required = false) String plugins);
}
