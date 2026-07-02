package com.demo.ticket.repository;

import com.demo.ticket.model.TicketEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketEventRepository extends JpaRepository<TicketEvent, Long> {

    /** 租户隔离：只查当前租户的活动 */
    List<TicketEvent> findByTenantIdOrderByIdAsc(String tenantId);

    Optional<TicketEvent> findByIdAndTenantId(Long id, String tenantId);

    /**
     * CAS 扣减库存：仅当 available_stock >= quantity 且 version 匹配时更新。
     * <p>
     * 【设计要点】数据库层乐观锁扣减，与 Redis 预占形成两阶段方案的第二阶段。
     * 若 affected rows = 0，说明并发冲突或库存不足，需回滚 Redis 预占。
     * </p>
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE TicketEvent e
            SET e.availableStock = e.availableStock - :quantity,
                e.version = e.version + 1
            WHERE e.id = :eventId
              AND e.tenantId = :tenantId
              AND e.availableStock >= :quantity
              AND e.version = :expectedVersion
            """)
    int deductStockCas(
            @Param("eventId") Long eventId,
            @Param("tenantId") String tenantId,
            @Param("quantity") int quantity,
            @Param("expectedVersion") long expectedVersion);

    /** 取消订单时归还库存 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE TicketEvent e
            SET e.availableStock = e.availableStock + :quantity
            WHERE e.id = :eventId AND e.tenantId = :tenantId
            """)
    int restoreStock(
            @Param("eventId") Long eventId,
            @Param("tenantId") String tenantId,
            @Param("quantity") int quantity);
}
