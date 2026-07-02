package com.demo.ticket.dto;

import com.demo.ticket.tenant.IsolationMode;
import com.demo.ticket.tenant.TenantTier;

/**
 * 租户信息 DTO — 跨服务传输与前端展示。
 */
public record TenantResponse(
        String tenantId,
        String name,
        TenantTier tier,
        IsolationMode isolationMode,
        int maxQps,
        String enabledPlugins
) {
}
