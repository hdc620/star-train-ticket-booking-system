package com.star.service;

public interface TicketService {

    /**
     * 原子扣减库存
     * @param scheduleId 车次ID
     * @param quantity   购买数量
     * @return 剩余库存；-1库存不足；-2缓存Key不存在
     */
    Long deductStock(Long scheduleId, int quantity);

    /**
     * 回滚库存（用于异常补偿）
     */
    void incrementStock(Long scheduleId, int quantity);

    /**
     * 初始化/预热库存
     */
    void initStock(Long scheduleId, Long totalSeats);

    /**
     * 清理 Redis 库存缓存（售票结束后调用）
     */
    void clearStockCache(Long scheduleId);
}
