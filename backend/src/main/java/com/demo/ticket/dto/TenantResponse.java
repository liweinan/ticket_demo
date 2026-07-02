package com.demo.ticket.dto;

import com.demo.ticket.model.Tenant;
import com.demo.ticket.tenant.IsolationMode;
import com.demo.ticket.tenant.TenantTier;

/**
 * 租户信息 DTO — 返回给前端用于切换租户与展示隔离模式。
 */
public record TenantResponse(
        String tenantId,
        String name,
        TenantTier tier,
        IsolationMode isolationMode,
        int maxQps,
        String enabledPlugins
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getTenantId(),
                tenant.getName(),
                tenant.getTier(),
                tenant.getIsolationMode(),
                tenant.getMaxQps(),
                tenant.getEnabledPlugins()
        );
    }
}
