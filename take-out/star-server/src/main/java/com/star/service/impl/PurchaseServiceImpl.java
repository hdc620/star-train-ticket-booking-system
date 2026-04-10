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
    private static final Random RANDOM = new Random();

    /**
     * 购票（优化版）
     */
    @Transactional(rollbackFor = Exception.class)
    public void save(PurchaseDTO purchaseDTO) {
        // ==============================
        // 1. 【关键优化】只查询1次车次
        // ==============================
        Long scheduleId = purchaseDTO.getScheduleId();
        Schedule schedule = scheduleMapper.selectById(scheduleId);

        // 【非空校验】防止空指针
        if (schedule == null) {
            throw new BaseException("车次信息不存在");
        }

        // ==============================
        // 2. 业务校验
        // ==============================
        // 2.1 判断车次是否可售票
        if (!StatusConstant.SCHEDULE_AVAILABLE.equals(schedule.getStatus())) {
            throw new BaseException("车次不在售票中，购票失败");
        }

        // 2.2 判断列车是否已经发车
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(schedule.getDepartureTime())) {
            throw new BaseException("列车已发车，购票失败");
        }

        // 2.3 判断剩余座位
        Long ticketCount = purchaseDTO.getTicketCount();
        Long remainingSeats = schedule.getRemainingSeats();
        if (remainingSeats < ticketCount) {
            throw new BaseException("车次剩余座位数不足，购票失败");
        }

        // ==============================
        // 3. 构建订单对象
        // ==============================
        Order order = new Order();
        BeanUtils.copyProperties(purchaseDTO, order);

        // 3.1 计算总金额
        BigDecimal totalAmount = schedule.getTicketPrice().multiply(new BigDecimal(ticketCount));
        order.setTotalAmount(totalAmount);

        // 3.2 设置用户ID
        Long userId = BaseContext.getCurrentId();
        order.setUserId(userId);

        // 3.3 生成订单号
        order.setOrderNo(generateOrderNo());

        // 3.4 设置订单状态
        order.setStatus(StatusConstant.ORDER_PAID);

        // ==============================
        // 4. 【关键修复】更新座位数到数据库
        // ==============================
        Long newRemainingSeats = remainingSeats - ticketCount;
        schedule.setRemainingSeats(newRemainingSeats);
        scheduleMapper.updateById(schedule); // 必须调用 updateById！

        // ==============================
        // 5. 插入订单
        // ==============================
        purchaseMapper.insert(order);
    }

    /**
     * 生成订单号（提取成私有方法，更整洁）
     */
    private String generateOrderNo() {
        String prefix = "ORD";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timePart = sdf.format(new Date());
        int randomPart = RANDOM.nextInt(9000) + 1000;
        return prefix + timePart + randomPart;
    }
}
