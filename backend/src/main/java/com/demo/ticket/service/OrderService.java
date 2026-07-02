package com.demo.ticket.service;

import com.demo.ticket.dto.CreateOrderRequest;
import com.demo.ticket.dto.OrderResponse;
import com.demo.ticket.id.SnowflakeIdGenerator;
import com.demo.ticket.inventory.InventoryService;
import com.demo.ticket.model.Order;
import com.demo.ticket.model.TicketEvent;
import com.demo.ticket.order.OrderState;
import com.demo.ticket.order.OrderStateMachine;
import com.demo.ticket.plugin.OrderPlugin;
import com.demo.ticket.plugin.OrderPluginContext;
import com.demo.ticket.plugin.PluginRegistry;
import com.demo.ticket.repository.OrderRepository;
import com.demo.ticket.repository.TicketEventRepository;
import com.demo.ticket.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

/**
 * 订单业务服务 — 编排状态机、库存、插件与持久化。
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final TicketEventRepository eventRepository;
    private final InventoryService inventoryService;
    private final OrderStateMachine stateMachine;
    private final SnowflakeIdGenerator idGenerator;
    private final PluginRegistry pluginRegistry;

    public OrderService(
            OrderRepository orderRepository,
            TicketEventRepository eventRepository,
            InventoryService inventoryService,
            OrderStateMachine stateMachine,
            SnowflakeIdGenerator idGenerator,
            PluginRegistry pluginRegistry) {
        this.orderRepository = orderRepository;
        this.eventRepository = eventRepository;
        this.inventoryService = inventoryService;
        this.stateMachine = stateMachine;
        this.idGenerator = idGenerator;
        this.pluginRegistry = pluginRegistry;
    }

    public List<OrderResponse> listOrders() {
        String tenantId = TenantContext.requireTenantId();
        return orderRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    /**
     * 创建订单（下单）。
     * <p>
     * 完整流程：插件 before → 库存两阶段扣减 → 写订单 → 插件 after。
     * </p>
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String tenantId = TenantContext.requireTenantId();

        TicketEvent event = eventRepository.findByIdAndTenantId(request.eventId(), tenantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "活动不存在或不属于当前租户"));

        if (request.quantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量必须大于 0");
        }

        OrderPluginContext pluginContext = new OrderPluginContext(
                tenantId, event.getId(), request.quantity());

        // 执行租户已启用插件的 before 钩子
        for (OrderPlugin plugin : pluginRegistry.activePlugins()) {
            plugin.beforeCreate(pluginContext);
        }

        // 两阶段库存扣减
        if (!inventoryService.deduct(event, request.quantity())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "库存不足，请减少数量或稍后再试");
        }

        Instant now = Instant.now();
        Order order = new Order();
        order.setId(idGenerator.nextId());
        order.setTenantId(tenantId);
        order.setEventId(event.getId());
        order.setEventTitle(event.getTitle());
        order.setQuantity(request.quantity());
        order.setState(OrderState.PENDING_PAYMENT);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setExtensionSnapshot(pluginContext.getExtensionSnapshot());

        Order saved = orderRepository.save(order);
        log.info("[Order] 创建订单 id={} tenant={} event={} qty={}",
                saved.getId(), tenantId, event.getId(), request.quantity());

        for (OrderPlugin plugin : pluginRegistry.activePlugins()) {
            plugin.afterCreate(saved, pluginContext);
        }

        return OrderResponse.from(saved);
    }

    /** 模拟支付：PENDING_PAYMENT → PAID */
    @Transactional
    public OrderResponse payOrder(Long orderId) {
        Order order = loadTenantOrder(orderId);
        order.setState(stateMachine.transition(order.getState(), OrderState.PAID));
        order.setUpdatedAt(Instant.now());
        return OrderResponse.from(orderRepository.save(order));
    }

    /** 出票：PAID → TICKET_ISSUED */
    @Transactional
    public OrderResponse issueTicket(Long orderId) {
        Order order = loadTenantOrder(orderId);
        order.setState(stateMachine.transition(order.getState(), OrderState.TICKET_ISSUED));
        order.setUpdatedAt(Instant.now());
        return OrderResponse.from(orderRepository.save(order));
    }

    /** 取消：PENDING_PAYMENT → CANCELLED，并归还库存 */
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        String tenantId = TenantContext.requireTenantId();
        Order order = loadTenantOrder(orderId);
        order.setState(stateMachine.transition(order.getState(), OrderState.CANCELLED));
        order.setUpdatedAt(Instant.now());

        TicketEvent event = eventRepository.findByIdAndTenantId(order.getEventId(), tenantId)
                .orElseThrow();
        inventoryService.restore(event, order.getQuantity());

        return OrderResponse.from(orderRepository.save(order));
    }

    private Order loadTenantOrder(Long orderId) {
        String tenantId = TenantContext.requireTenantId();
        return orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在"));
    }
}
