package com.demo.ticket.inventory;

import com.demo.ticket.model.TicketEvent;
import com.demo.ticket.repository.TicketEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 库存服务：编排「Redis 预占 + 数据库 CAS」两阶段扣减。
 * <p>
 * 【高并发库存扣减 - 核心流程】
 * <ol>
 *   <li>Redis/内存原子预占 — 快速挡掉明显超卖</li>
 *   <li>DB CAS 扣减（带 version）— 持久化与最终一致</li>
 *   <li>若 DB 失败，回滚 Redis 预占</li>
 * </ol>
 * </p>
 */
@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryCache inventoryCache;
    private final TicketEventRepository eventRepository;

    public InventoryService(InventoryCache inventoryCache, TicketEventRepository eventRepository) {
        this.inventoryCache = inventoryCache;
        this.eventRepository = eventRepository;
    }

    /** 缓存键格式：inventory:{tenantId}:{eventId} */
    public static String cacheKey(String tenantId, Long eventId) {
        return "inventory:" + tenantId + ":" + eventId;
    }

    /**
     * 应用启动或首次访问时，将 DB 库存同步到缓存层。
     */
    public void ensureCacheSynced(TicketEvent event) {
        inventoryCache.syncStock(cacheKey(event.getTenantId(), event.getId()), event.getAvailableStock());
    }

    /**
     * 两阶段扣减库存。
     *
     * @return true 扣减成功；false 库存不足或并发冲突
     */
    public boolean deduct(TicketEvent event, int quantity) {
        String key = cacheKey(event.getTenantId(), event.getId());
        ensureCacheSynced(event);

        // 阶段 1：缓存预占
        if (!inventoryCache.tryReserve(key, quantity)) {
            log.warn("[Inventory] 缓存预占失败 eventId={} qty={}", event.getId(), quantity);
            return false;
        }

        // 阶段 2：数据库 CAS
        int updated = eventRepository.deductStockCas(
                event.getId(),
                event.getTenantId(),
                quantity,
                event.getVersion());

        if (updated == 0) {
            // DB 扣减失败，回滚缓存
            inventoryCache.release(key, quantity);
            log.warn("[Inventory] DB CAS 失败，已回滚缓存 eventId={} version={}",
                    event.getId(), event.getVersion());
            return false;
        }

        log.info("[Inventory] 扣减成功 eventId={} qty={}", event.getId(), quantity);
        return true;
    }

    /** 取消订单时归还库存（DB + 缓存） */
    public void restore(TicketEvent event, int quantity) {
        eventRepository.restoreStock(event.getId(), event.getTenantId(), quantity);
        inventoryCache.release(cacheKey(event.getTenantId(), event.getId()), quantity);
    }
}
