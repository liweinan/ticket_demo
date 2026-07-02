package com.demo.ticket.dto;

/**
 * 网关/微服务聚合健康检查响应。
 */
public record HealthResponse(
        String status,
        String message,
        boolean postgresUp,
        boolean redisUp,
        boolean nacosUp,
        boolean kafkaUp
) {
    public static HealthResponse of(boolean postgresUp, boolean redisUp, boolean nacosUp, boolean kafkaUp) {
        boolean coreUp = postgresUp && redisUp;
        String status = coreUp ? "UP" : "DEGRADED";
        String message = coreUp
                ? "ticket-saas 分布式集群运行正常"
                : "部分基础设施不可用";
        return new HealthResponse(status, message, postgresUp, redisUp, nacosUp, kafkaUp);
    }

    /** 单服务简化健康（无 Redis/Kafka 时传 false） */
    public static HealthResponse simple(String serviceName, boolean postgresUp) {
        return new HealthResponse(
                postgresUp ? "UP" : "DOWN",
                serviceName + (postgresUp ? " 正常" : " 数据库不可用"),
                postgresUp,
                false,
                false,
                false);
    }
}
