package com.demo.ticket.dto;

import com.demo.ticket.model.Order;
import com.demo.ticket.order.OrderState;

import java.time.Instant;

/**
 * 订单 DTO。
 * <p>
 * {@code id} 使用 String：Snowflake 64 位 ID 超出 JS {@code Number.MAX_SAFE_INTEGER}，
 * JSON 数字会导致前端精度丢失，支付/出票等操作会 404。
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
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                String.valueOf(order.getId()),
                order.getTenantId(),
                order.getEventId(),
                order.getEventTitle(),
                order.getQuantity(),
                order.getState(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getExtensionSnapshot()
        );
    }
}
