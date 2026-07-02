package com.demo.ticket.controller;

import com.demo.ticket.dto.HealthResponse;
import com.demo.ticket.dto.TenantResponse;
import com.demo.ticket.service.TenantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class TenantApiController {

    private final TenantService tenantService;
    private final DataSource dataSource;

    public TenantApiController(TenantService tenantService, DataSource dataSource) {
        this.tenantService = tenantService;
        this.dataSource = dataSource;
    }

    /** 内部 API — 供 Gateway 校验租户存在性与加载配额 */
    @GetMapping("/internal/tenants/{tenantId}")
    public TenantResponse getTenantInternal(@PathVariable String tenantId) {
        return tenantService.getTenant(tenantId);
    }

    @GetMapping("/api/health")
    public HealthResponse health() {
        return HealthResponse.simple("tenant-service", checkPostgres());
    }

    private boolean checkPostgres() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception ex) {
            return false;
        }
    }
}
