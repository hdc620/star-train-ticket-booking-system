package com.star.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.star.constant.StatusConstant;
import com.star.entity.Order;
import com.star.entity.Schedule;
import com.star.mapper.AdminOrderMapper;
import com.star.mapper.ScheduleMapper;
import com.star.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理车次和订单状态
 */
@Component
@Slf4j
public class MyTask {
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private AdminOrderMapper adminOrderMapper;

    /**
     * 处理车次和订单状态的方法
     */
    @Scheduled(cron = "0 * * * * ? ") //每分钟触发一次
    public void schedule() {
        LocalDateTime now = LocalDateTime.now();
        String status= StatusConstant.SCHEDULE_DEPARTED;
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(Schedule::getDepartureTime,now)
                .ne(Schedule::getStatus,status);
        List<Schedule> scheduleList= scheduleMapper.selectList(queryWrapper);
        if(scheduleList.size()>0){
            for(Schedule schedule:scheduleList){
                schedule.setStatus(status);

                //处理订单
                Long sheduleId=schedule.getScheduleId();
                String statusOrder=StatusConstant.ORDER_COMPLETED;
                LambdaQueryWrapper<Order> orderQueryWrapper = new LambdaQueryWrapper<>();
                orderQueryWrapper.eq(Order::getScheduleId,sheduleId)
                        .ne(Order::getStatus,statusOrder);
                List<Order> orderList=adminOrderMapper.selectList(orderQueryWrapper);
                if(orderList.size()>0){
                    for(Order order:orderList){
                        order.setStatus(statusOrder);
                    }
                }
            }
        }
    }
}
