package com.demo.ticket.controller;

import com.demo.ticket.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class OrderHealthController {

    private final DataSource dataSource;

    public OrderHealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    public HealthResponse health() {
        return HealthResponse.simple("order-service", checkPostgres());
    }

    private boolean checkPostgres() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception ex) {
            return false;
        }
    }
}
