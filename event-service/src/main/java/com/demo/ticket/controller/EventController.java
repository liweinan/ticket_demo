package com.demo.ticket.controller;

import com.demo.ticket.dto.EventResponse;
import com.demo.ticket.service.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> listEvents() {
        return eventService.listEventsForCurrentTenant();
    }

    @GetMapping("/{eventId}")
    public EventResponse getEvent(@PathVariable Long eventId) {
        return eventService.getEvent(eventId);
    }
}
