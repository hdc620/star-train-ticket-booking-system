package com.star.service.impl;

import com.star.entity.Schedule;
import com.star.mapper.ScheduleMapper;
import com.star.service.TicketService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
public class TicketServiceImpl implements TicketService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ScheduleMapper scheduleMapper;  // 用于懒加载时查询DB库存

    private DefaultRedisScript<Long> deductScript;

    @PostConstruct
    public void init() {
        deductScript = new DefaultRedisScript<>();
        deductScript.setLocation(new ClassPathResource("lua/deduct_stock.lua"));
        deductScript.setResultType(Long.class);
    }

    @Override
    public Long deductStock(Long scheduleId, int quantity) {
        String stockKey = "ticket:stock:" + scheduleId;
        Long result = stringRedisTemplate.execute(
                deductScript,
                Collections.singletonList(stockKey),
                String.valueOf(quantity)
        );

        // 懒加载：如果Key不存在，尝试从数据库加载并重试一次
        if (result != null && result == -2) {
            boolean initialized = tryInitStockFromDB(scheduleId);
            if (initialized) {
                result = stringRedisTemplate.execute(
                        deductScript,
                        Collections.singletonList(stockKey),
                        String.valueOf(quantity)
                );
            }
        }
        return result;
    }

    /**
     * 安全地从数据库加载库存到 Redis（使用 SETNX 防并发）
     */
    private boolean tryInitStockFromDB(Long scheduleId) {
        String stockKey = "ticket:stock:" + scheduleId;
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            return false;
        }
        Long remaining = schedule.getRemainingSeats();
        // 仅当 Key 不存在时才设置
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(stockKey, String.valueOf(remaining));
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void incrementStock(Long scheduleId, int quantity) {
        String stockKey = "ticket:stock:" + scheduleId;
        stringRedisTemplate.opsForValue().increment(stockKey, quantity);
    }

    @Override
    public void initStock(Long scheduleId, Long totalSeats) {
        String stockKey = "ticket:stock:" + scheduleId;
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(totalSeats));
        log.info("Redis库存初始化完成，scheduleId: {}, 总座位数: {}", scheduleId, totalSeats);
    }

    @Override
    public void clearStockCache(Long scheduleId) {
        String stockKey = "ticket:stock:" + scheduleId;
        Boolean deleted = stringRedisTemplate.delete(stockKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("Redis库存缓存已清理，scheduleId: {}", scheduleId);
        } else {
            log.debug("Redis库存缓存不存在或已清理，scheduleId: {}", scheduleId);
        }
    }
}
