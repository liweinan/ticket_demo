package com.demo.ticket.controller;

import com.demo.ticket.dto.CreateOrderRequest;
import com.demo.ticket.dto.OrderResponse;
import com.demo.ticket.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单 API — 演示状态机驱动的下单/支付/出票/取消流程。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> listOrders() {
        return orderService.listOrders();
    }

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    /** 模拟支付成功 */
    @PostMapping("/{orderId}/pay")
    public OrderResponse pay(@PathVariable Long orderId) {
        return orderService.payOrder(orderId);
    }

    /** 模拟出票 */
    @PostMapping("/{orderId}/issue")
    public OrderResponse issue(@PathVariable Long orderId) {
        return orderService.issueTicket(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }
}
