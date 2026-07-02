package com.demo.ticket.plugin;

import com.demo.ticket.tenant.TenantContext;
import com.demo.ticket.tenant.TenantInfo;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 插件注册中心：根据当前租户 enabled_plugins 筛选要执行的插件。
 */
@Component
public class PluginRegistry {

    private final Map<String, OrderPlugin> pluginMap;

    public PluginRegistry(List<OrderPlugin> plugins) {
        this.pluginMap = plugins.stream()
                .collect(Collectors.toMap(OrderPlugin::id, Function.identity()));
    }

    /** 获取当前租户已启用的插件列表 */
    public List<OrderPlugin> activePlugins() {
        TenantInfo tenant = TenantContext.get();
        if (tenant == null || tenant.enabledPlugins() == null || tenant.enabledPlugins().isBlank()) {
            return List.of();
        }
        return Arrays.stream(tenant.enabledPlugins().split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .map(pluginMap::get)
                .filter(p -> p != null)
                .toList();
    }
}
