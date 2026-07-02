package com.demo.ticket.plugin;

/**
 * 插件执行上下文：在核心下单流程与插件之间传递数据。
 */
public class OrderPluginContext {

    private final String tenantId;
    private final Long eventId;
    private final int quantity;
    private String extensionSnapshot;

    public OrderPluginContext(String tenantId, Long eventId, int quantity) {
        this.tenantId = tenantId;
        this.eventId = eventId;
        this.quantity = quantity;
        this.extensionSnapshot = "{}";
    }

    public String getTenantId() {
        return tenantId;
    }

    public Long getEventId() {
        return eventId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getExtensionSnapshot() {
        return extensionSnapshot;
    }

    public void setExtensionSnapshot(String extensionSnapshot) {
        this.extensionSnapshot = extensionSnapshot;
    }
}
