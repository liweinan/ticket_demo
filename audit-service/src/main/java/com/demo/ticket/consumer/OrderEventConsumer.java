package com.demo.ticket.consumer;

import com.demo.ticket.constants.TenantConstants;
import com.demo.ticket.event.OrderEvent;
import com.demo.ticket.model.OrderAuditLog;
import com.demo.ticket.repository.OrderAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 订单事件消费者 — 演示 Kafka 异步解耦与审计落库。
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final OrderAuditLogRepository auditLogRepository;

    public OrderEventConsumer(OrderAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @KafkaListener(topics = TenantConstants.ORDER_EVENTS_TOPIC, groupId = "audit-service-group")
    public void onOrderEvent(OrderEvent event) {
        OrderAuditLog logEntry = new OrderAuditLog();
        logEntry.setEventType(event.eventType().name());
        logEntry.setOrderId(event.orderId());
        logEntry.setTenantId(event.tenantId());
        logEntry.setEventId(event.eventId());
        logEntry.setQuantity(event.quantity());
        logEntry.setOrderState(event.orderState());
        logEntry.setOccurredAt(event.timestamp());
        logEntry.setReceivedAt(Instant.now());
        auditLogRepository.save(logEntry);
        log.info("[Audit] 记录订单事件 type={} orderId={} tenant={}",
                event.eventType(), event.orderId(), event.tenantId());
    }
}
