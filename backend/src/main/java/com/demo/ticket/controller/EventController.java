package com.demo.ticket.controller;

import com.demo.ticket.dto.EventResponse;
import com.demo.ticket.dto.TenantResponse;
import com.demo.ticket.service.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 活动 API — 租户隔离的数据访问入口。
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /** 当前租户的活动列表（需 X-Tenant-ID） */
    @GetMapping
    public List<EventResponse> listEvents() {
        return eventService.listEventsForCurrentTenant();
    }

    @GetMapping("/{eventId}")
    public EventResponse getEvent(@PathVariable Long eventId) {
        return eventService.getEvent(eventId);
    }
}
