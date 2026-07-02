package com.demo.ticket.dto;

/**
 * 库存归还内部请求（order-service → event-service）。
 */
public record InventoryRestoreRequest(
        Long eventId,
        int quantity
) {
}
