package com.demo.ticket.dto;

import com.demo.ticket.model.TicketEvent;

/**
 * 票务活动 DTO。
 */
public record EventResponse(
        Long id,
        String tenantId,
        String title,
        String venue,
        int totalStock,
        int availableStock,
        long version,
        String extensionFields
) {
    public static EventResponse from(TicketEvent event) {
        return new EventResponse(
                event.getId(),
                event.getTenantId(),
                event.getTitle(),
                event.getVenue(),
                event.getTotalStock(),
                event.getAvailableStock(),
                event.getVersion(),
                event.getExtensionFields()
        );
    }
}
