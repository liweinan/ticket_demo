package com.demo.ticket.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内存版库存预占实现（Redis 不可用时的降级方案）。
 * <p>
 * Demo 默认使用此实现，无需启动 Redis 即可体验完整下单流程。
 * 生产环境应替换为 {@link RedisInventoryCache}，利用 Redis DECR 的原子性。
 * </p>
 */
@Component
public class InMemoryInventoryCache implements InventoryCache {

    private static final Logger log = LoggerFactory.getLogger(InMemoryInventoryCache.class);

    private final Map<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();

    @Override
    public boolean tryReserve(String cacheKey, int quantity) {
        AtomicInteger stock = stockMap.computeIfAbsent(cacheKey, k -> new AtomicInteger(0));
        while (true) {
            int current = stock.get();
            if (current < quantity) {
                return false;
            }
            if (stock.compareAndSet(current, current - quantity)) {
                log.debug("[InventoryCache/Memory] 预占成功 key={} qty={} remain={}",
                        cacheKey, quantity, current - quantity);
                return true;
            }
        }
    }

    @Override
    public void release(String cacheKey, int quantity) {
        stockMap.computeIfAbsent(cacheKey, k -> new AtomicInteger(0)).addAndGet(quantity);
        log.debug("[InventoryCache/Memory] 释放预占 key={} qty={}", cacheKey, quantity);
    }

    @Override
    public void syncStock(String cacheKey, int availableStock) {
        stockMap.put(cacheKey, new AtomicInteger(availableStock));
    }
}
