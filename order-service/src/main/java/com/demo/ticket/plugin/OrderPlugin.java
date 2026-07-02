package com.demo.ticket.plugin;

import com.demo.ticket.model.Order;

/**
 * 订单插件接口 — 「核心标准化，定制插件化」。
 * <p>
 * 不同租户可启用不同插件（审批流、特殊校验、自定义通知等），
 * 而核心下单逻辑保持稳定。插件通过 {@link PluginRegistry} 按租户配置加载。
 * </p>
 */
public interface OrderPlugin {

    /** 插件唯一 ID，与 tenants.enabled_plugins 对应 */
    String id();

    /** 插件描述，供管理后台展示 */
    String description();

    /**
     * 下单前钩子：可拦截（抛异常）或补充扩展数据。
     *
     * @param context 下单上下文
     */
    void beforeCreate(OrderPluginContext context);

    /**
     * 下单后钩子：发送通知、触发审批等。
     */
    void afterCreate(Order order, OrderPluginContext context);
}
