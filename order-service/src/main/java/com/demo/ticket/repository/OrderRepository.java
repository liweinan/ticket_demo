package com.demo.ticket.repository;

import com.demo.ticket.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    Optional<Order> findByIdAndTenantId(Long id, String tenantId);
}
