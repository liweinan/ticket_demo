package com.demo.ticket.controller;

import com.demo.ticket.constants.TenantConstants;
import com.demo.ticket.dto.EventResponse;
import com.demo.ticket.dto.HealthResponse;
import com.demo.ticket.dto.InventoryDeductRequest;
import com.demo.ticket.dto.InventoryRestoreRequest;
import com.demo.ticket.inventory.InventoryService;
import com.demo.ticket.model.TicketEvent;
import com.demo.ticket.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 内部 API — 仅服务间 Feign 调用，从 Header 读取租户（不依赖 ThreadLocal）。
 */
@RestController
public class EventInternalController {

    private final EventService eventService;
    private final InventoryService inventoryService;
    private final DataSource dataSource;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    public EventInternalController(
            EventService eventService,
            InventoryService inventoryService,
            DataSource dataSource,
            org.springframework.data.redis.core.StringRedisTemplate redisTemplate) {
        this.eventService = eventService;
        this.inventoryService = inventoryService;
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/internal/events/{eventId}")
    public EventResponse getEventInternal(
            @PathVariable Long eventId,
            @RequestHeader(TenantConstants.TENANT_HEADER) String tenantId) {
        return eventService.getEventForTenant(eventId, tenantId.trim());
    }

    @PostMapping("/internal/inventory/deduct")
    public boolean deduct(
            @RequestBody InventoryDeductRequest request,
            @RequestHeader(TenantConstants.TENANT_HEADER) String tenantId) {
        TicketEvent event = eventService.loadEventForTenant(request.eventId(), tenantId.trim());
        if (event.getVersion() != request.expectedVersion()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "活动版本已变更，请刷新后重试");
        }
        return inventoryService.deduct(event, request.quantity());
    }

    @PostMapping("/internal/inventory/restore")
    public void restore(
            @RequestBody InventoryRestoreRequest request,
            @RequestHeader(TenantConstants.TENANT_HEADER) String tenantId) {
        TicketEvent event = eventService.loadEventForTenant(request.eventId(), tenantId.trim());
        inventoryService.restore(event, request.quantity());
    }

    @GetMapping("/api/health")
    public HealthResponse health() {
        return new HealthResponse(
                checkPostgres() && checkRedis() ? "UP" : "DEGRADED",
                "event-service",
                checkPostgres(),
                checkRedis(),
                false,
                false);
    }

    private boolean checkPostgres() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean checkRedis() {
        try (var connection = redisTemplate.getConnectionFactory().getConnection()) {
            return "PONG".equalsIgnoreCase(connection.ping());
        } catch (Exception ex) {
            return false;
        }
    }
}
