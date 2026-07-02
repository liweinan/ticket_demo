package com.demo.ticket.controller;

import com.demo.ticket.dto.TenantResponse;
import com.demo.ticket.service.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 租户 API — 列出 Demo 中的三个租户供前端切换。
 * <p>
 * 此接口不要求 X-Tenant-ID（在拦截器 exclude 之外需单独处理）。
 * 实际生产环境租户列表通常来自登录用户的授权范围。
 * </p>
 */
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final EventService eventService;

    public TenantController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<TenantResponse> listTenants() {
        return eventService.listTenants();
    }
}
