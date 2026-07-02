package com.demo.ticket.event;

/**
 * 订单 Kafka 事件类型。
 */
public enum OrderEventType {
    ORDER_CREATED,
    ORDER_PAID,
    ORDER_ISSUED,
    ORDER_CANCELLED
}
