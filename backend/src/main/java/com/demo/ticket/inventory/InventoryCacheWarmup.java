package com.demo.ticket.inventory;

import com.demo.ticket.model.TicketEvent;
import com.demo.ticket.repository.TicketEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动后将 PostgreSQL 中的活动库存同步到 Redis。
 * <p>
 * 保证 Redis 预占层与数据库初始库存一致；下单过程中以 DB CAS 为最终权威。
 * </p>
 */
@Component
public class InventoryCacheWarmup {

    private static final Logger log = LoggerFactory.getLogger(InventoryCacheWarmup.class);

    private final TicketEventRepository eventRepository;
    private final InventoryCache inventoryCache;

    public InventoryCacheWarmup(TicketEventRepository eventRepository, InventoryCache inventoryCache) {
        this.eventRepository = eventRepository;
        this.inventoryCache = inventoryCache;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpRedisFromDatabase() {
        int count = 0;
        for (TicketEvent event : eventRepository.findAll()) {
            inventoryCache.syncStock(
                    InventoryService.cacheKey(event.getTenantId(), event.getId()),
                    event.getAvailableStock());
            count++;
        }
        log.info("[InventoryCacheWarmup] 已将 {} 个活动库存同步至 Redis", count);
    }
}
