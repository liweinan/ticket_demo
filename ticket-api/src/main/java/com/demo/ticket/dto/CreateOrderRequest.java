package com.demo.ticket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 创建订单请求体。
 */
public record CreateOrderRequest(
        @NotNull Long eventId,
        @Min(1) int quantity
) {
}
