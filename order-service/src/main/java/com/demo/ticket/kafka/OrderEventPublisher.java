package com.demo.ticket.kafka;

import com.demo.ticket.constants.TenantConstants;
import com.demo.ticket.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单领域事件发布器 — 将状态变更异步广播至 Kafka。
 */
@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(OrderEvent event) {
        kafkaTemplate.send(TenantConstants.ORDER_EVENTS_TOPIC, event.tenantId(), event);
        log.info("[Kafka] 发布订单事件 type={} orderId={} tenant={}",
                event.eventType(), event.orderId(), event.tenantId());
    }
}
