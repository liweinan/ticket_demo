package com.demo.ticket.dto;

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
}
