package com.star.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.constant.StatusConstant;
import com.star.entity.Order;
import com.star.entity.Schedule;
import com.star.exception.BaseException;
import com.star.mapper.RefundMapper;
import com.star.mapper.ScheduleMapper;
import com.star.service.RefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RefundServiceImpl extends ServiceImpl<RefundMapper, Order> implements RefundService {
    @Autowired
    private RefundMapper refundMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * 退票
     */
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long id) {
        // 1. 只查询1次订单（杜绝重复查询）
        Order order = refundMapper.selectById(id);
        if (order == null) {
            throw new BaseException("订单不存在");
        }

        // 2. 校验订单状态
        String status = order.getStatus();
        if (status.equals(StatusConstant.ORDER_REFUNDED)) {
            throw new BaseException("已退票的票是无法重复退票的哦");
        }
        if (status.equals(StatusConstant.ORDER_COMPLETED)) {
            throw new BaseException("您的票上的车次已经发车了，无法退票了");
        }

        // 3. 修改订单状态为已退款
        order.setStatus(StatusConstant.ORDER_REFUNDED);
        // ✅ 关键：更新订单到数据库
        refundMapper.updateById(order);

        // 4. 恢复车次剩余座位
        Schedule schedule = scheduleMapper.selectById(order.getScheduleId());
        if (schedule == null) {
            throw new BaseException("车次信息不存在");
        }
        // 退票后座位数 = 原座位 + 退票数量
        long newRemainingSeats = schedule.getRemainingSeats() + order.getTicketCount();
        schedule.setRemainingSeats(newRemainingSeats);
        // ✅ 关键：更新车次座位到数据库
        scheduleMapper.updateById(schedule);
    }
}
