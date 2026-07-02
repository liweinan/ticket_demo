package com.demo.ticket.tenant;

/**
 * 租户付费等级，决定 API 限流配额。
 * <p>
 * 金/银/铜牌租户在 API 网关层可设置不同速率限制，
 * 防止「吵闹的邻居」拖垮共享资源。
 * </p>
 */
public enum TenantTier {

    /** 金牌：最高 QPS，适合大客户 */
    GOLD(100),

    /** 银牌：中等 QPS */
    SILVER(30),

    /** 铜牌：基础 QPS，适合小微租户 */
    BRONZE(10);

    private final int defaultMaxQps;

    TenantTier(int defaultMaxQps) {
        this.defaultMaxQps = defaultMaxQps;
    }

    public int getDefaultMaxQps() {
        return defaultMaxQps;
    }
}
