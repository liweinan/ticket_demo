package com.demo.ticket.tenant;

/**
 * 租户上下文：基于 ThreadLocal 在请求链路中透传租户身份。
 * <p>
 * 【设计要点】从 API 网关到微服务，租户标识必须在整条调用链中保持一致。
 * 本 Demo 在 Servlet 拦截器入口设置，在 finally 中清理，避免线程池复用导致串租户。
 * </p>
 * <p>
 * 生产环境可改为：
 * <ul>
 *   <li>从 JWT claim 解析 tenantId（{@code X-Tenant-ID} 仅作网关校验后的透传）</li>
 *   <li>结合 MDC 写入 traceId + tenantId 便于日志检索</li>
 * </ul>
 * </p>
 */
public final class TenantContext {

    private static final ThreadLocal<TenantInfo> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(TenantInfo tenant) {
        CURRENT.set(tenant);
    }

    public static TenantInfo get() {
        return CURRENT.get();
    }

    /**
     * 获取当前租户 ID；若未设置则抛出异常，防止无租户上下文时误查全表数据。
     */
    public static String requireTenantId() {
        TenantInfo info = CURRENT.get();
        if (info == null || info.tenantId() == null || info.tenantId().isBlank()) {
            throw new IllegalStateException("缺少租户上下文，请携带 X-Tenant-ID 请求头");
        }
        return info.tenantId();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
