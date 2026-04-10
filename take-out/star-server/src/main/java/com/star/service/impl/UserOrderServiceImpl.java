package com.star.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.context.BaseContext;
import com.star.dto.UserOrderPageQueryDTO;
import com.star.entity.Order;
import com.star.entity.Schedule;
import com.star.entity.Train;
import com.star.mapper.ScheduleMapper;
import com.star.mapper.TrainMapper;
import com.star.mapper.UserOrderMapper;
import com.star.result.PageResult;
import com.star.service.UserOrderService;
import com.star.vo.UserOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserOrderServiceImpl extends ServiceImpl<UserOrderMapper, Order> implements UserOrderService {
    @Autowired
    private UserOrderMapper userOrderMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private TrainMapper trainMapper;

    /**
     * 用户订单分页查询
     * @param queryDTO 分页
     * @return 分页结果
     */
    public PageResult pageQuery(UserOrderPageQueryDTO queryDTO) {
        // 1. 创建分页对象
        Page<Order> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 2. 构建条件构造器
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

        String status = queryDTO.getStatus();

        // 【关键修改】只有 status 不为 null 且 不为空字符串时，才拼接条件
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq(Order::getStatus, status);
        }

        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            queryWrapper.eq(Order::getUserId, userId);
        }

        // 【优化】按创建时间倒序，最新的订单在最前面
        queryWrapper.orderByDesc(Order::getCreateTime);

        // 6. 执行分页查询（使用BaseMapper的selectPage方法）
        Page<Order> resultPage = userOrderMapper.selectPage(page, queryWrapper);

        // 7. 转换为VO并封装分页结果
        List<UserOrderVO> voList = resultPage.getRecords().stream()
                .map(order -> {
                    UserOrderVO vo = new UserOrderVO();
                    BeanUtils.copyProperties(order, vo);
                    Schedule schedule = scheduleMapper.selectById(order.getScheduleId());
                    Train train=trainMapper.selectById(schedule.getTrainId());
                    String trainCode = train.getTrainCode();
                    vo.setTrainCode(trainCode);
                    return vo;
                })
                .collect(Collectors.toList());

        PageResult pageResult = new PageResult();
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setRecords(voList);

        return pageResult;
    }
}
