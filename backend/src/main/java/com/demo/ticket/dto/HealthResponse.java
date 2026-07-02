package com.demo.ticket.dto;

/**
 * 健康检查响应。
 */
public record HealthResponse(
        String status,
        String message
) {
    public static HealthResponse ok() {
        return new HealthResponse("UP", "ticket-saas-demo 运行正常");
    }
}
