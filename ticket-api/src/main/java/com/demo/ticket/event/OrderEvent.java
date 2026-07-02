package com.demo.ticket.event;

import java.time.Instant;

/**
 * 订单领域事件 — 通过 Kafka 异步广播状态变更。
 */
public record OrderEvent(
        OrderEventType eventType,
        String orderId,
        String tenantId,
        Long eventId,
        int quantity,
        String orderState,
        Instant timestamp
) {
}
