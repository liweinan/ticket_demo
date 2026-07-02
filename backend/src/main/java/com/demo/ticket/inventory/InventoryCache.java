package com.demo.ticket.inventory;

/**
 * 库存预占缓存抽象。
 * <p>
 * 【设计要点 - 两阶段扣减】
 * 第一阶段：在 Redis 中原子递减预占库存，快速拒绝超卖请求；
 * 第二阶段：在数据库中用 CAS + 乐观锁持久化，保证最终一致性。
 * </p>
 */
public interface InventoryCache {

    /**
     * 尝试预占库存（原子操作）。
     *
     * @param cacheKey 缓存键，通常含 tenantId + eventId
     * @param quantity 预占数量
     * @return true 预占成功；false 库存不足
     */
    boolean tryReserve(String cacheKey, int quantity);

    /**
     * 释放预占（下单失败或取消订单时调用）。
     */
    void release(String cacheKey, int quantity);

    /** 初始化/同步缓存库存（应用启动或 DB 变更后） */
    void syncStock(String cacheKey, int availableStock);
}
