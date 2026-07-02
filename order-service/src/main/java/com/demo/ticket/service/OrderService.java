package com.demo.ticket.service;

import com.demo.ticket.api.EventInventoryClient;
import com.demo.ticket.api.EventQueryClient;
import com.demo.ticket.dto.CreateOrderRequest;
import com.demo.ticket.dto.EventResponse;
import com.demo.ticket.dto.InventoryDeductRequest;
import com.demo.ticket.dto.InventoryRestoreRequest;
import com.demo.ticket.dto.OrderResponse;
import com.demo.ticket.event.OrderEvent;
import com.demo.ticket.event.OrderEventType;
import com.demo.ticket.id.SnowflakeIdGenerator;
import com.demo.ticket.kafka.OrderEventPublisher;
import com.demo.ticket.model.Order;
import com.demo.ticket.order.OrderState;
import com.demo.ticket.order.OrderStateMachine;
import com.demo.ticket.plugin.OrderPlugin;
import com.demo.ticket.plugin.OrderPluginContext;
import com.demo.ticket.plugin.PluginRegistry;
import com.demo.ticket.repository.OrderRepository;
import com.demo.ticket.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final EventQueryClient eventQueryClient;
    private final EventInventoryClient eventInventoryClient;
    private final OrderStateMachine stateMachine;
    private final SnowflakeIdGenerator idGenerator;
    private final PluginRegistry pluginRegistry;
    private final OrderEventPublisher eventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            EventQueryClient eventQueryClient,
            EventInventoryClient eventInventoryClient,
            OrderStateMachine stateMachine,
            SnowflakeIdGenerator idGenerator,
            PluginRegistry pluginRegistry,
            OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventQueryClient = eventQueryClient;
        this.eventInventoryClient = eventInventoryClient;
        this.stateMachine = stateMachine;
        this.idGenerator = idGenerator;
        this.pluginRegistry = pluginRegistry;
        this.eventPublisher = eventPublisher;
    }

    public List<OrderResponse> listOrders() {
        String tenantId = TenantContext.requireTenantId();
        return orderRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String tenantId = TenantContext.requireTenantId();
        TenantHeaders headers = tenantHeaders();

        EventResponse event = eventQueryClient.getEvent(request.eventId(), headers.tenantId(), headers.tier(), headers.plugins());
        if (!tenantId.equals(event.tenantId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在或不属于当前租户");
        }

        if (request.quantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量必须大于 0");
        }

        OrderPluginContext pluginContext = new OrderPluginContext(
                tenantId, event.id(), request.quantity());

        for (OrderPlugin plugin : pluginRegistry.activePlugins()) {
            plugin.beforeCreate(pluginContext);
        }

        boolean deducted = eventInventoryClient.deduct(
                new InventoryDeductRequest(event.id(), request.quantity(), event.version()),
                headers.tenantId(), headers.tier(), headers.plugins());
        if (!deducted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "库存不足，请减少数量或稍后再试");
        }

        Instant now = Instant.now();
        Order order = new Order();
        order.setId(idGenerator.nextId());
        order.setTenantId(tenantId);
        order.setEventId(event.id());
        order.setEventTitle(event.title());
        order.setQuantity(request.quantity());
        order.setState(OrderState.PENDING_PAYMENT);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setExtensionSnapshot(pluginContext.getExtensionSnapshot());

        Order saved = orderRepository.save(order);
        log.info("[Order] 创建订单 id={} tenant={} event={} qty={}",
                saved.getId(), tenantId, event.id(), request.quantity());

        for (OrderPlugin plugin : pluginRegistry.activePlugins()) {
            plugin.afterCreate(saved, pluginContext);
        }

        publishEvent(OrderEventType.ORDER_CREATED, saved);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse payOrder(Long orderId) {
        Order order = loadTenantOrder(orderId);
        order.setState(stateMachine.transition(order.getState(), OrderState.PAID));
        order.setUpdatedAt(Instant.now());
        Order saved = orderRepository.save(order);
        publishEvent(OrderEventType.ORDER_PAID, saved);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse issueTicket(Long orderId) {
        Order order = loadTenantOrder(orderId);
        order.setState(stateMachine.transition(order.getState(), OrderState.TICKET_ISSUED));
        order.setUpdatedAt(Instant.now());
        Order saved = orderRepository.save(order);
        publishEvent(OrderEventType.ORDER_ISSUED, saved);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = loadTenantOrder(orderId);
        order.setState(stateMachine.transition(order.getState(), OrderState.CANCELLED));
        order.setUpdatedAt(Instant.now());

        TenantHeaders headers = tenantHeaders();
        eventInventoryClient.restore(
                new InventoryRestoreRequest(order.getEventId(), order.getQuantity()),
                headers.tenantId(), headers.tier(), headers.plugins());

        Order saved = orderRepository.save(order);
        publishEvent(OrderEventType.ORDER_CANCELLED, saved);
        return toResponse(saved);
    }

    private void publishEvent(OrderEventType type, Order order) {
        eventPublisher.publish(new OrderEvent(
                type,
                String.valueOf(order.getId()),
                order.getTenantId(),
                order.getEventId(),
                order.getQuantity(),
                order.getState().name(),
                Instant.now()));
    }

    private Order loadTenantOrder(Long orderId) {
        String tenantId = TenantContext.requireTenantId();
        return orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在"));
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                String.valueOf(order.getId()),
                order.getTenantId(),
                order.getEventId(),
                order.getEventTitle(),
                order.getQuantity(),
                order.getState(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getExtensionSnapshot());
    }

    private TenantHeaders tenantHeaders() {
        var info = TenantContext.get();
        String tenantId = TenantContext.requireTenantId();
        String tier = info != null && info.tier() != null ? info.tier().name() : null;
        String plugins = info != null ? info.enabledPlugins() : "";
        return new TenantHeaders(tenantId, tier, plugins);
    }

    private record TenantHeaders(String tenantId, String tier, String plugins) {
    }
}
