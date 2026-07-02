package com.demo.ticket.controller;

import com.demo.ticket.dto.HealthResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 健康检查 — 不经过租户拦截器；探测 PostgreSQL 与 Redis 连通性。
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;
    private final StringRedisTemplate redisTemplate;

    public HealthController(DataSource dataSource, StringRedisTemplate redisTemplate) {
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping
    public HealthResponse health() {
        return HealthResponse.of(checkPostgres(), checkRedis());
    }

    private boolean checkPostgres() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean checkRedis() {
        try (var connection = redisTemplate.getConnectionFactory().getConnection()) {
            return "PONG".equalsIgnoreCase(connection.ping());
        } catch (Exception ex) {
            return false;
        }
    }
}
