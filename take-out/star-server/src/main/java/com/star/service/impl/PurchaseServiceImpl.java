package com.star.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.constant.StatusConstant;
import com.star.context.BaseContext;
import com.star.dto.PurchaseDTO;
import com.star.entity.Order;
import com.star.entity.Schedule;
import com.star.exception.BaseException;
import com.star.mapper.PurchaseMapper;
import com.star.mapper.ScheduleMapper;
import com.star.service.PurchaseService;
import com.star.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 购票业务实现类
 * 使用 Redis + Lua 原子扣减库存，结合分布式锁防止重复下单
 */
@Service
@Slf4j
public class PurchaseServiceImpl extends ServiceImpl<PurchaseMapper, Order> implements PurchaseService {

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private RedissonClient redissonClient;

    // 使用雪花算法生成订单号（推荐），此处为示例保留原方法
    // 如需升级，可注入 SnowflakeIdWorker 或使用 Hutool 的 IdUtil
    // @Autowired
    // private SnowflakeIdWorker idWorker;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(PurchaseDTO purchaseDTO) {
        Long scheduleId = purchaseDTO.getScheduleId();
        Long ticketCount = purchaseDTO.getTicketCount();
        Long userId = BaseContext.getCurrentId();

        // ==================== 1. 基础业务校验 ====================
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BaseException("车次信息不存在");
        }
        if (!StatusConstant.SCHEDULE_AVAILABLE.equals(schedule.getStatus())) {
            throw new BaseException("车次不在售票中，购票失败");
        }
        if (LocalDateTime.now().isAfter(schedule.getDepartureTime())) {
            throw new BaseException("列车已发车，购票失败");
        }

        // ==================== 2. Redis + Lua 原子扣减库存 ====================
        Long remain = ticketService.deductStock(scheduleId, ticketCount.intValue());
        if (remain == -2) {
            throw new BaseException("车次库存缓存不存在，请联系管理员初始化");
        }
        if (remain == -1) {
            throw new BaseException("车次剩余座位数不足，购票失败");
        }

        // ==================== 3. 同步更新数据库余票 ====================
        int affectedRows = scheduleMapper.decrementRemainingSeats(scheduleId, ticketCount);
        if (affectedRows == 0) {
            // 极少数情况下 Redis 扣减成功但 DB 余票不足，补偿 Redis 库存
            ticketService.incrementStock(scheduleId, ticketCount.intValue());
            log.error("数据库扣减余票失败，已补偿Redis库存。scheduleId: {}, 购买数: {}", scheduleId, ticketCount);
            throw new BaseException("系统繁忙，请稍后重试");
        }

        // ==================== 4. 分布式锁防重复下单 ====================
        // 锁粒度：用户ID + 车次ID，确保同一用户对同一车次只能生成一个有效订单
        String lockKey = "order:lock:" + userId + ":" + scheduleId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试加锁：等待0秒，持有时间10秒（根据业务耗时调整）
            boolean locked = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (!locked) {
                // 没拿到锁，说明有并发请求正在处理
                throw new BaseException("请求处理中，请稍后查看订单");
            }

            try {
                // 双重检查：确认锁内订单是否已存在（防止极端情况下的重复写入）
                if (orderExists(userId, scheduleId)) {
                    throw new BaseException("您已购买过该车次，请勿重复下单");
                }

                // 生成订单
                Order order = new Order();
                BeanUtils.copyProperties(purchaseDTO, order);
                order.setTotalAmount(schedule.getTicketPrice().multiply(new BigDecimal(ticketCount)));
                order.setUserId(userId);
                order.setOrderNo(generateOrderNo());  // 可替换为雪花算法
                order.setStatus(StatusConstant.ORDER_PAID);
                order.setCreateTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());

                purchaseMapper.insert(order);
                log.info("订单创建成功，订单号：{}，用户：{}，车次：{}", order.getOrderNo(), userId, scheduleId);

            } finally {
                // 释放锁（仅当锁被当前线程持有且未过期时）
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BaseException("系统繁忙，请稍后重试");
        }
    }

    /**
     * 检查用户是否已存在该车次的有效订单（防止重复下单）
     * 有效订单状态：已支付、已完成（已退票的订单允许再次购买）
     */
    private boolean orderExists(Long userId, Long scheduleId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
                .eq(Order::getScheduleId, scheduleId)
                .in(Order::getStatus, StatusConstant.ORDER_PAID, StatusConstant.ORDER_COMPLETED);
        return purchaseMapper.selectCount(wrapper) > 0;
    }

    /**
     * 生成订单号（示例方法，高并发下建议使用雪花算法或Redis自增）
     */
    private String generateOrderNo() {
        // 简单示例：ORD + 时间戳 + 随机数（存在极小概率重复）
        String prefix = "ORD";
        String timePart = String.valueOf(System.currentTimeMillis());
        int randomPart = (int) (Math.random() * 9000) + 1000;
        return prefix + timePart + randomPart;
    }

    // 如果使用雪花算法生成订单号，可替换为如下方式：
    // private String generateOrderNo() {
    //     return String.valueOf(idWorker.nextId());
    // }
}
