package com.demo.ticket.service;

import com.demo.ticket.dto.EventResponse;
import com.demo.ticket.dto.TenantResponse;
import com.demo.ticket.model.Tenant;
import com.demo.ticket.model.TicketEvent;
import com.demo.ticket.repository.TenantRepository;
import com.demo.ticket.repository.TicketEventRepository;
import com.demo.ticket.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租户与活动查询服务。
 */
@Service
public class EventService {

    private final TicketEventRepository eventRepository;
    private final TenantRepository tenantRepository;

    public EventService(TicketEventRepository eventRepository, TenantRepository tenantRepository) {
        this.eventRepository = eventRepository;
        this.tenantRepository = tenantRepository;
    }

    /** 列出所有租户（供前端切换，无需 X-Tenant-ID） */
    public List<TenantResponse> listTenants() {
        return tenantRepository.findAllByOrderByTenantIdAsc().stream()
                .map(TenantResponse::from)
                .toList();
    }

    /** 当前租户下的活动列表（必须带租户上下文） */
    public List<EventResponse> listEventsForCurrentTenant() {
        String tenantId = TenantContext.requireTenantId();
        return eventRepository.findByTenantIdOrderByIdAsc(tenantId).stream()
                .map(EventResponse::from)
                .toList();
    }

    public EventResponse getEvent(Long eventId) {
        String tenantId = TenantContext.requireTenantId();
        TicketEvent event = eventRepository.findByIdAndTenantId(eventId, tenantId)
                .orElseThrow();
        return EventResponse.from(event);
    }
}
