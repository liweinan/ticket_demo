package com.demo.ticket.order;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 订单状态机：集中管理合法状态变迁。
 * <p>
 * 典型流转：
 * <pre>
 *   PENDING_PAYMENT → PAID → TICKET_ISSUED
 *   PENDING_PAYMENT → CANCELLED
 *   PAID → REFUNDED
 *   TICKET_ISSUED → REFUNDED
 * </pre>
 * 复杂场景（改签）可扩展为独立事件驱动模块，本 Demo 覆盖核心路径。
 * </p>
 */
@Component
public class OrderStateMachine {

    private static final Map<OrderState, Set<OrderState>> TRANSITIONS = new EnumMap<>(OrderState.class);

    static {
        TRANSITIONS.put(OrderState.PENDING_PAYMENT, EnumSet.of(OrderState.PAID, OrderState.CANCELLED));
        TRANSITIONS.put(OrderState.PAID, EnumSet.of(OrderState.TICKET_ISSUED, OrderState.REFUNDED));
        TRANSITIONS.put(OrderState.TICKET_ISSUED, EnumSet.of(OrderState.REFUNDED));
        TRANSITIONS.put(OrderState.CANCELLED, EnumSet.noneOf(OrderState.class));
        TRANSITIONS.put(OrderState.REFUNDED, EnumSet.noneOf(OrderState.class));
    }

    /**
     * 校验并返回目标状态；非法变迁抛出 409 Conflict。
     */
    public OrderState transition(OrderState current, OrderState target) {
        Set<OrderState> allowed = TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderState.class));
        if (!allowed.contains(target)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "非法状态变迁: " + current + " → " + target);
        }
        return target;
    }

    /** 是否为终态（不可再流转） */
    public boolean isTerminal(OrderState state) {
        return TRANSITIONS.getOrDefault(state, EnumSet.noneOf(OrderState.class)).isEmpty();
    }
}
