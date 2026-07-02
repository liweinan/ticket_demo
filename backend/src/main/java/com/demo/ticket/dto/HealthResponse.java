package com.demo.ticket.dto;

/**
 * 健康检查响应。
 */
public record HealthResponse(
        String status,
        String message,
        boolean postgresUp,
        boolean redisUp
) {
    public static HealthResponse of(boolean postgresUp, boolean redisUp) {
        boolean up = postgresUp && redisUp;
        return new HealthResponse(
                up ? "UP" : "DEGRADED",
                up ? "ticket-saas-demo 运行正常" : "PostgreSQL 或 Redis 不可用",
                postgresUp,
                redisUp);
    }
}
