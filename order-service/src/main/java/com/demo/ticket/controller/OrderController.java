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

    @PostMapping("/{orderId}/pay")
    public OrderResponse pay(@PathVariable Long orderId) {
        return orderService.payOrder(orderId);
    }

    @PostMapping("/{orderId}/issue")
    public OrderResponse issue(@PathVariable Long orderId) {
        return orderService.issueTicket(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }
}
