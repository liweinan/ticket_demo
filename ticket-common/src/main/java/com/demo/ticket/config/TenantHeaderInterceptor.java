package com.demo.ticket.config;

import com.demo.ticket.constants.TenantConstants;
import com.demo.ticket.tenant.IsolationMode;
import com.demo.ticket.tenant.TenantContext;
import com.demo.ticket.tenant.TenantInfo;
import com.demo.ticket.tenant.TenantTier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 微服务侧租户拦截器：从网关透传的 Header 写入 {@link TenantContext}。
 * <p>
 * 限流已在 Gateway 层由 Resilience4j 完成；此处不再重复限流。
 * </p>
 */
public class TenantHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader(TenantConstants.TENANT_HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "缺少请求头: " + TenantConstants.TENANT_HEADER);
        }

        TenantTier tier = parseTier(request.getHeader(TenantConstants.TENANT_TIER_HEADER));
        String plugins = request.getHeader(TenantConstants.TENANT_PLUGINS_HEADER);
        if (plugins == null) {
            plugins = "";
        }

        TenantInfo info = new TenantInfo(
                tenantId.trim(),
                tenantId.trim(),
                tier,
                IsolationMode.SHARED_TABLE,
                tier.getDefaultMaxQps(),
                plugins
        );
        TenantContext.set(info);
        return true;
    }

    private TenantTier parseTier(String raw) {
        if (raw == null || raw.isBlank()) {
            return TenantTier.BRONZE;
        }
        try {
            return TenantTier.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TenantTier.BRONZE;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}
