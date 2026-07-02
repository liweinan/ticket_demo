package com.demo.ticket.gateway.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 租户等级 QPS 配额 — 可通过 Nacos 动态刷新。
 */
@Component
@ConfigurationProperties(prefix = "tenant.rate-limit")
public class TenantRateLimitProperties {

    private int gold = 100;
    private int silver = 30;
    private int bronze = 10;
    private int defaultQps = 10;

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getSilver() {
        return silver;
    }

    public void setSilver(int silver) {
        this.silver = silver;
    }

    public int getBronze() {
        return bronze;
    }

    public void setBronze(int bronze) {
        this.bronze = bronze;
    }

    public int getDefaultQps() {
        return defaultQps;
    }

    public void setDefaultQps(int defaultQps) {
        this.defaultQps = defaultQps;
    }

    public int resolveMaxQps(com.demo.ticket.tenant.TenantTier tier, int tenantMaxQps) {
        if (tenantMaxQps > 0) {
            return tenantMaxQps;
        }
        if (tier == null) {
            return defaultQps;
        }
        return switch (tier) {
            case GOLD -> gold;
            case SILVER -> silver;
            case BRONZE -> bronze;
        };
    }
}
