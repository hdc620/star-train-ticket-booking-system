package com.star.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.star.constant.StatusConstant;
import com.star.entity.Order;
import com.star.entity.Schedule;
import com.star.mapper.AdminOrderMapper;
import com.star.mapper.ScheduleMapper;
import com.star.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class MyTask {

    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private AdminOrderMapper adminOrderMapper;
    @Autowired
    private TicketService ticketService;  // 注入 TicketService

    /**
     * 每分钟处理已发车车次的状态，并清理 Redis 库存缓存
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void schedule() {
        LocalDateTime now = LocalDateTime.now();
        String departedStatus = StatusConstant.SCHEDULE_DEPARTED;
        String completedStatus = StatusConstant.ORDER_COMPLETED;

        // 查询已发车但状态未更新的车次
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(Schedule::getDepartureTime, now)
                .ne(Schedule::getStatus, departedStatus);
        List<Schedule> scheduleList = scheduleMapper.selectList(queryWrapper);

        if (scheduleList.isEmpty()) {
            return;
        }

        for (Schedule schedule : scheduleList) {
            Long scheduleId = schedule.getScheduleId();

            // 1. 更新车次状态
            schedule.setStatus(departedStatus);
            scheduleMapper.updateById(schedule);

            // 2. 更新该车次下未完成的订单状态
            LambdaQueryWrapper<Order> orderQueryWrapper = new LambdaQueryWrapper<>();
            orderQueryWrapper.eq(Order::getScheduleId, scheduleId)
                    .ne(Order::getStatus, completedStatus);
            List<Order> orderList = adminOrderMapper.selectList(orderQueryWrapper);
            if (!orderList.isEmpty()) {
                for (Order order : orderList) {
                    order.setStatus(completedStatus);
                    adminOrderMapper.updateById(order);
                }
            }

            // 3. 【新增】清理 Redis 库存缓存
            ticketService.clearStockCache(scheduleId);
        }
    }
}
