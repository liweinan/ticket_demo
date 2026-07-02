package com.demo.ticket.model;

import com.demo.ticket.tenant.IsolationMode;
import com.demo.ticket.tenant.TenantTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 租户元数据实体。
 * <p>
 * 存储租户等级、隔离模式声明、限流配额、已启用插件等「平台级」配置。
 * 业务表（活动、订单）通过 {@code tenant_id} 关联，不在此做外键级联，
 * 便于未来按隔离模式将租户数据迁移到独立库。
 * </p>
 */
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TenantTier tier;

    /**
     * 声明的隔离模式。Demo 运行时统一走共享表；真实系统据此路由 DataSource。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "isolation_mode", nullable = false, length = 32)
    private IsolationMode isolationMode;

    /** 每秒最大 API 请求数（租户级限流） */
    @Column(name = "max_qps", nullable = false)
    private int maxQps;

    /** 逗号分隔的插件 ID，如 {@code approval-workflow} */
    @Column(name = "enabled_plugins", length = 512)
    private String enabledPlugins;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TenantTier getTier() {
        return tier;
    }

    public void setTier(TenantTier tier) {
        this.tier = tier;
    }

    public IsolationMode getIsolationMode() {
        return isolationMode;
    }

    public void setIsolationMode(IsolationMode isolationMode) {
        this.isolationMode = isolationMode;
    }

    public int getMaxQps() {
        return maxQps;
    }

    public void setMaxQps(int maxQps) {
        this.maxQps = maxQps;
    }

    public String getEnabledPlugins() {
        return enabledPlugins;
    }

    public void setEnabledPlugins(String enabledPlugins) {
        this.enabledPlugins = enabledPlugins;
    }
}
