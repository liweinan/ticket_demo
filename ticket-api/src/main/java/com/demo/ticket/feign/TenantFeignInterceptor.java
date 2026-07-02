package com.demo.ticket.feign;

import com.demo.ticket.constants.TenantConstants;
import com.demo.ticket.tenant.TenantContext;
import com.demo.ticket.tenant.TenantInfo;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器：自动透传 {@code X-Tenant-ID} 到下游微服务。
 * <p>
 * CircuitBreaker 可能在独立线程执行 Feign 调用，ThreadLocal 不可用，
 * 因此回退到 {@link RequestContextHolder} 读取当前 HTTP 请求头。
 * </p>
 */
public class TenantFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        TenantInfo info = TenantContext.get();
        if (info != null && info.tenantId() != null) {
            applyHeaders(template, info.tenantId(), tierName(info), info.enabledPlugins());
            return;
        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String tenantId = request.getHeader(TenantConstants.TENANT_HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            return;
        }
        applyHeaders(
                template,
                tenantId,
                request.getHeader(TenantConstants.TENANT_TIER_HEADER),
                request.getHeader(TenantConstants.TENANT_PLUGINS_HEADER));
    }

    private void applyHeaders(RequestTemplate template, String tenantId, String tier, String plugins) {
        template.header(TenantConstants.TENANT_HEADER, tenantId);
        if (tier != null && !tier.isBlank()) {
            template.header(TenantConstants.TENANT_TIER_HEADER, tier);
        }
        if (plugins != null) {
            template.header(TenantConstants.TENANT_PLUGINS_HEADER, plugins);
        }
    }

    private String tierName(TenantInfo info) {
        return info.tier() != null ? info.tier().name() : null;
    }
}
