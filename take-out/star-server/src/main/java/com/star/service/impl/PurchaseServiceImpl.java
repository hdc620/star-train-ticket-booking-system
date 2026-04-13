package com.star.service.impl;

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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@Service
@Slf4j
public class PurchaseServiceImpl extends ServiceImpl<PurchaseMapper, Order> implements PurchaseService {

    @Autowired
    private PurchaseMapper purchaseMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private TicketService ticketService;

    private static final Random RANDOM = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(PurchaseDTO purchaseDTO) {
        Long scheduleId = purchaseDTO.getScheduleId();
        Long ticketCount = purchaseDTO.getTicketCount();

        // ========== 1. 基础业务校验 ==========
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

        // ========== 2. Redis 原子扣减 ==========
        Long remain = ticketService.deductStock(scheduleId, ticketCount.intValue());
        if (remain == -2) {
            throw new BaseException("车次库存缓存不存在，请联系管理员初始化");
        }
        if (remain == -1) {
            throw new BaseException("车次剩余座位数不足，购票失败");
        }

        // ========== 3. 同步更新数据库余票 ==========
        int affectedRows = scheduleMapper.decrementRemainingSeats(scheduleId, ticketCount);
        if (affectedRows == 0) {
            // 极少数情况：Redis 扣减成功但 DB 余票不足（数据不一致），需要补偿 Redis
            ticketService.incrementStock(scheduleId, ticketCount.intValue());
            log.error("数据库扣减余票失败，scheduleId: {}, 购买数: {}", scheduleId, ticketCount);
            throw new BaseException("系统繁忙，请稍后重试");
        }

        // ========== 4. 生成订单 ==========
        Order order = new Order();
        BeanUtils.copyProperties(purchaseDTO, order);
        order.setTotalAmount(schedule.getTicketPrice().multiply(new BigDecimal(ticketCount)));
        order.setUserId(BaseContext.getCurrentId());
        order.setOrderNo(generateOrderNo());
        order.setStatus(StatusConstant.ORDER_PAID);
        purchaseMapper.insert(order);
    }

    private String generateOrderNo() {
        String prefix = "ORD";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timePart = sdf.format(new Date());
        int randomPart = RANDOM.nextInt(9000) + 1000;
        return prefix + timePart + randomPart;
    }
}
