package com.demo.ticket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 票务活动实体（共享表模式，{@code tenant_id} 逻辑隔离）。
 * <p>
 * 【扩展字段设计】{@code extensionFields} 以 JSON 存储租户个性化配置
 * （座位图、实名要求等），避免为每个租户改表结构。
 * 使用 PostgreSQL {@code jsonb} 列类型，可建 GIN 索引加速查询。
 * </p>
 * <p>
 * 【乐观锁】{@code version} 配合 CAS 扣减库存，防止并发超卖。
 * </p>
 */
@Entity
@Table(name = "ticket_events")
public class TicketEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 租户 ID — 所有查询必须带此条件，防止跨租户数据泄露 */
    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(nullable = false, length = 256)
    private String venue;

    @Column(name = "total_stock", nullable = false)
    private int totalStock;

    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    /** JPA 乐观锁版本号，每次更新 +1 */
    @Version
    private long version;

    /** 租户扩展字段（PostgreSQL jsonb） */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extension_fields", columnDefinition = "jsonb")
    private String extensionFields = "{}";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getExtensionFields() {
        return extensionFields;
    }

    public void setExtensionFields(String extensionFields) {
        this.extensionFields = extensionFields;
    }
}
