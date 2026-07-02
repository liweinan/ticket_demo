package com.demo.ticket.tenant;

/**
 * 当前请求绑定的租户运行时信息。
 * <p>
 * 由 {@link TenantInterceptor} 从 HTTP Header {@code X-Tenant-ID} 解析后写入
 * {@link TenantContext}，供 Service / Repository 层读取，确保所有数据访问
 * 自动带上租户约束。
 * </p>
 *
 * @param tenantId      租户唯一标识
 * @param name          租户展示名
 * @param tier          付费等级（决定限流）
 * @param isolationMode 声明的隔离模式（Demo 中主要用于展示）
 * @param maxQps        该租户每秒最大请求数
 * @param enabledPlugins 逗号分隔的已启用插件 ID
 */
public record TenantInfo(
        String tenantId,
        String name,
        TenantTier tier,
        IsolationMode isolationMode,
        int maxQps,
        String enabledPlugins
) {
}
