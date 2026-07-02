package com.demo.ticket.ratelimit;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 租户级 API 限流器（内存滑动窗口实现）。
 * <p>
 * 【设计要点 - 防噪声邻居】
 * 在共享资源环境下，单个租户的突发流量不应影响其他租户。
 * 生产环境建议在 API 网关（Kong / Envoy / Spring Cloud Gateway）统一限流；
 * 应用层保留此组件作为第二道防线，并与租户等级（金/银/铜）配额联动。
 * </p>
 * <p>
 * 算法：滑动窗口 — 记录最近 1 秒内的时间戳，超过 {@code maxQps} 则拒绝。
 * </p>
 */
@Component
public class TenantRateLimiter {

    /** tenantId → 最近请求时间戳队列 */
    private final Map<String, Deque<Long>> windows = new ConcurrentHashMap<>();

    /**
     * 尝试消耗一次配额。
     *
     * @param tenantId 租户 ID
     * @param maxQps   该租户允许的最大 QPS
     * @throws ResponseStatusException 429 超出配额
     */
    public void acquire(String tenantId, int maxQps) {
        long now = System.currentTimeMillis();
        Deque<Long> window = windows.computeIfAbsent(tenantId, k -> new ArrayDeque<>());

        synchronized (window) {
            // 移除 1 秒窗口外的旧记录
            while (!window.isEmpty() && now - window.peekFirst() > 1000) {
                window.pollFirst();
            }
            if (window.size() >= maxQps) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "租户 " + tenantId + " 超出 QPS 限制 (" + maxQps + "/s)，请稍后重试");
            }
            window.addLast(now);
        }
    }
}
