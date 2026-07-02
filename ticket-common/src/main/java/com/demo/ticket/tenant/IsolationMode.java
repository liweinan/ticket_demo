package com.demo.ticket.tenant;

/**
 * 租户数据隔离模式枚举。
 * <p>
 * 多租户 SaaS 常见的三种数据隔离与存储模型。
 * 本 Demo 在应用层统一实现 {@link IsolationMode#SHARED_TABLE} 的读写隔离；
 * 其他模式在此声明，供路由层/运维脚本在真实 SaaS 中切换数据源或 Schema。
 * </p>
 */
public enum IsolationMode {

    /**
     * 独立数据库：物理隔离最强，适合金融/大型客户。
     * 生产环境需为每个租户维护独立 DataSource 路由。
     */
    DEDICATED_DB,

    /**
     * 共享库、独立 Schema：成本与隔离性的折中。
     * 可通过 Hibernate multi-tenancy SCHEMA 策略实现。
     */
    SHARED_SCHEMA,

    /**
     * 共享库、共享表 + tenant_id：成本最低，海量小租户常用。
     * 本 Demo 默认实现此模式。
     */
    SHARED_TABLE
}
