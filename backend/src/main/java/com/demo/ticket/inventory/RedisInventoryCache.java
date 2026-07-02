package com.demo.ticket.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

/**
 * Redis 库存预占实现 — 两阶段扣减的第一阶段。
 * <p>
 * 使用 Lua 脚本保证「读-判断-写」原子性，等价于生产环境的 Redis 预占语义：
 * 仅当库存充足时才 {@code DECRBY}，避免并发超卖。
 * </p>
 */
@Component
public class RedisInventoryCache implements InventoryCache {

    private static final Logger log = LoggerFactory.getLogger(RedisInventoryCache.class);

    /**
     * 原子预占脚本：
     * <ul>
     *   <li>返回 1 — 预占成功</li>
     *   <li>返回 0 — 库存不足</li>
     *   <li>返回 -1 — key 未初始化（需先 syncStock）</li>
     * </ul>
     */
    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
            """
                    local stock = redis.call('GET', KEYS[1])
                    if stock == false then return -1 end
                    stock = tonumber(stock)
                    local qty = tonumber(ARGV[1])
                    if stock < qty then return 0 end
                    redis.call('DECRBY', KEYS[1], qty)
                    return 1
                    """,
            Long.class);

    private static final Duration KEY_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public RedisInventoryCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryReserve(String cacheKey, int quantity) {
        Long result = redisTemplate.execute(
                RESERVE_SCRIPT,
                Collections.singletonList(cacheKey),
                String.valueOf(quantity));

        if (result == null || result == -1) {
            log.warn("[InventoryCache/Redis] key 未初始化 key={}", cacheKey);
            return false;
        }
        if (result == 0) {
            log.debug("[InventoryCache/Redis] 预占失败（库存不足） key={} qty={}", cacheKey, quantity);
            return false;
        }
        log.debug("[InventoryCache/Redis] 预占成功 key={} qty={}", cacheKey, quantity);
        return true;
    }

    @Override
    public void release(String cacheKey, int quantity) {
        redisTemplate.opsForValue().increment(cacheKey, quantity);
        log.debug("[InventoryCache/Redis] 释放预占 key={} qty={}", cacheKey, quantity);
    }

    @Override
    public void syncStock(String cacheKey, int availableStock) {
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(availableStock), KEY_TTL);
        log.debug("[InventoryCache/Redis] 同步库存 key={} stock={}", cacheKey, availableStock);
    }
}
