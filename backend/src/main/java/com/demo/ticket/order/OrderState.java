package com.demo.ticket.order;

/**
 * 订单状态枚举 — 票务业务状态机的所有合法节点。
 * <p>
 * 【设计要点 - 订单状态机】
 * 状态变迁必须严格受控，禁止业务代码随意 {@code setStatus}。
 * 所有流转通过 {@link OrderStateMachine#transition} 完成，并记录审计日志。
 * </p>
 */
public enum OrderState {

    /** 已创建，库存已预占，等待支付 */
    PENDING_PAYMENT,

    /** 支付成功，待出票 */
    PAID,

    /** 已出票 / 已完成 */
    TICKET_ISSUED,

    /** 用户或系统取消（释放库存） */
    CANCELLED,

    /** 已退款（从 PAID / TICKET_ISSUED 回退） */
    REFUNDED
}
