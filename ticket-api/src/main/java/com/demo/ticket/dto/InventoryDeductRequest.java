package com.demo.ticket.dto;

/**
 * 库存扣减内部请求（order-service → event-service）。
 */
public record InventoryDeductRequest(
        Long eventId,
        int quantity,
        long expectedVersion
) {
}
