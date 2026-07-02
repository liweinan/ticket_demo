package com.demo.ticket.dto;

import com.demo.ticket.order.OrderState;

import java.time.Instant;

/**
 * 订单 DTO。
 * <p>
 * {@code id} 使用 String：Snowflake 64 位 ID 超出 JS {@code Number.MAX_SAFE_INTEGER}。
 * </p>
 */
public record OrderResponse(
        String id,
        String tenantId,
        Long eventId,
        String eventTitle,
        int quantity,
        OrderState state,
        Instant createdAt,
        Instant updatedAt,
        String extensionSnapshot
) {
}
