package com.demo.ticket.tenant;

import com.demo.ticket.model.Tenant;
import com.demo.ticket.ratelimit.TenantRateLimiter;
import com.demo.ticket.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 租户身份识别拦截器。
 * <p>
 * 【设计要点 - 租户标识透传】
 * 每个 API 请求必须携带 {@code X-Tenant-ID}，拦截器负责：
 * <ol>
 *   <li>解析并校验租户是否存在</li>
 *   <li>写入 {@link TenantContext} 供下游使用</li>
 *   <li>触发租户级限流</li>
 * </ol>
 * 生产环境通常由 API 网关校验 JWT 中的 tenant claim，再以 Header 透传到微服务。
 * </p>
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    /** 租户标识 HTTP Header 名 */
    public static final String TENANT_HEADER = "X-Tenant-ID";

    private final TenantRepository tenantRepository;
    private final TenantRateLimiter rateLimiter;

    public TenantInterceptor(TenantRepository tenantRepository, TenantRateLimiter rateLimiter) {
        this.tenantRepository = tenantRepository;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少请求头: " + TENANT_HEADER);
        }

        Tenant tenant = tenantRepository.findById(tenantId.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "未知租户: " + tenantId));

        TenantInfo info = new TenantInfo(
                tenant.getTenantId(),
                tenant.getName(),
                tenant.getTier(),
                tenant.getIsolationMode(),
                tenant.getMaxQps(),
                tenant.getEnabledPlugins() == null ? "" : tenant.getEnabledPlugins()
        );

        // 限流：按租户配额消耗令牌
        rateLimiter.acquire(info.tenantId(), info.maxQps());

        TenantContext.set(info);
        log.debug("[Tenant] 请求绑定租户 tenantId={} tier={} isolation={}",
                info.tenantId(), info.tier(), info.isolationMode());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 必须清理 ThreadLocal，防止 Tomcat 线程池复用导致租户串数据
        TenantContext.clear();
    }
}
