package com.demo.ticket.order;

/**
 * 订单状态枚举 — 票务业务状态机的所有合法节点。
 */
public enum OrderState {

    PENDING_PAYMENT,
    PAID,
    TICKET_ISSUED,
    CANCELLED,
    REFUNDED
}
