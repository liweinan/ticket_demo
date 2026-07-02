package com.demo.ticket.service;

import com.demo.ticket.dto.EventResponse;
import com.demo.ticket.model.TicketEvent;
import com.demo.ticket.repository.TicketEventRepository;
import com.demo.ticket.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EventService {

    private final TicketEventRepository eventRepository;

    public EventService(TicketEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventResponse> listEventsForCurrentTenant() {
        String tenantId = TenantContext.requireTenantId();
        return eventRepository.findByTenantIdOrderByIdAsc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponse getEvent(Long eventId) {
        String tenantId = TenantContext.requireTenantId();
        return getEventForTenant(eventId, tenantId);
    }

    public EventResponse getEventForTenant(Long eventId, String tenantId) {
        TicketEvent event = eventRepository.findByIdAndTenantId(eventId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        return toResponse(event);
    }

    public TicketEvent loadEventForTenant(Long eventId, String tenantId) {
        return eventRepository.findByIdAndTenantId(eventId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
    }

    public EventResponse toResponse(TicketEvent event) {
        return new EventResponse(
                event.getId(),
                event.getTenantId(),
                event.getTitle(),
                event.getVenue(),
                event.getTotalStock(),
                event.getAvailableStock(),
                event.getVersion(),
                event.getExtensionFields());
    }
}
