package com.demo.ticket.repository;

import com.demo.ticket.model.OrderAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAuditLogRepository extends JpaRepository<OrderAuditLog, Long> {
}
