package com.demo.ticket.constants;

/**
 * 跨服务共享的 HTTP Header 与 Kafka 常量。
 */
public final class TenantConstants {

    /** 租户标识 HTTP Header 名 */
    public static final String TENANT_HEADER = "X-Tenant-ID";

    /** 网关校验后透传的租户等级（供下游限流/插件使用） */
    public static final String TENANT_TIER_HEADER = "X-Tenant-Tier";

    /** 网关透传的已启用插件列表（逗号分隔） */
    public static final String TENANT_PLUGINS_HEADER = "X-Tenant-Enabled-Plugins";

    /** 订单事件 Kafka Topic */
    public static final String ORDER_EVENTS_TOPIC = "order.events";

    private TenantConstants() {
    }
}
