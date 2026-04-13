package com.star.controller.admin;

import com.star.entity.Schedule;
import com.star.mapper.ScheduleMapper;
import com.star.service.TicketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stock")
@Tag(name = "redis预热相关接口", description = "管理员手动预热 Redis 库存的工具")
public class StockManageController {

    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private TicketService ticketService;

    @PostMapping("/init/{scheduleId}")
    public String initStock(@PathVariable Long scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            return "车次不存在";
        }
        ticketService.initStock(scheduleId, schedule.getRemainingSeats());
        return "Redis 库存初始化成功，当前余票：" + schedule.getRemainingSeats();
    }
}
