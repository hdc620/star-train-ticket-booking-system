package com.star.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.dto.AdminOrderDTO;
import com.star.dto.AdminOrderPageQueryDTO;
import com.star.dto.ScheduleDTO;
import com.star.dto.SchedulePageQueryDTO;
import com.star.entity.Schedule;
import com.star.entity.Train;
import com.star.entity.User;
import com.star.mapper.AdminOrderMapper;
import com.star.mapper.UserMapper;
import com.star.result.PageResult;
import com.star.service.AdminOrderService;
import com.star.entity.Order;
import com.star.service.UserService;
import com.star.vo.AdminOrderVO;
import com.star.vo.ScheduleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminOrderServiceImpl extends ServiceImpl<AdminOrderMapper, Order> implements AdminOrderService {
    @Autowired
    private AdminOrderMapper adminOrderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 订单分页查询（
     * @param queryDTO 分页
     * @return 分页结果
     */
    public PageResult pageQuery(AdminOrderPageQueryDTO queryDTO) {
        // 1. 创建分页对象
        Page<Order> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 2. 构建条件构造器
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

        String userName = queryDTO.getUserName();
        if(userName != null && !"".equals(userName)){
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getUserName, userName);
            User user = userMapper.selectOne(userLambdaQueryWrapper);
            if(user != null){
                queryWrapper.eq(Order::getUserId, user.getUserId());
            }
        }

        Long scheduleId = queryDTO.getScheduleId();
        if (scheduleId != null&&!scheduleId.equals(0L)) {
            queryWrapper.eq(Order::getScheduleId, scheduleId);
        }

        String status = queryDTO.getStatus();
        if (status != null) {
            queryWrapper.eq(Order::getStatus, status);
        }
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();
        if (startTime != null && endTime != null) {
            queryWrapper.between(Order::getCreateTime, startTime, endTime);
        }


        // 6. 执行分页查询（使用BaseMapper的selectPage方法）
        Page<Order> resultPage = adminOrderMapper.selectPage(page, queryWrapper);

        // 7. 转换为VO并封装分页结果

        // 1. 批量提取所有 useIds，避免循环查库
        List<Long> useIds = resultPage.getRecords().stream()
                .map(Order::getUserId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());

// 2. 批量查询 User 并转为 Map（仅需 1 次 SQL）
        Map<Long, User> userMap = useIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(useIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
// 3. 转换 VO 并处理异常情况
        List<AdminOrderVO> voList = resultPage.getRecords().stream()
                .map(order -> {
                    AdminOrderVO vo = new AdminOrderVO();
                    BeanUtils.copyProperties(order, vo);

                    // 安全获取 User 对象
                    User user = userMap.get(order.getUserId());
                    if (user != null && user.getUserName() != null) {
                        vo.setUserName(user.getUserName());
                    } else {
                        // 异常情况默认 null，避免崩溃
                        vo.setUserName("null");
                    }
                    String phone = user.getPhone();
                    if (phone != null && !"".equals(phone)) {
                        vo.setPhone(phone);
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        PageResult pageResult = new PageResult();
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setRecords(voList);

        return pageResult;
    }

    /**
     * 修改订单基本信息
     */
    @Transactional
    public void update(AdminOrderDTO adminOrderDTO) {
        Order order = new Order();
        BeanUtils.copyProperties(adminOrderDTO, order);

        //修改订单表基本信息
        adminOrderMapper.updateById(order);
    }

    /**
     * 订单批量删除
     */
    @Transactional
    public void deleteBatch(List<Integer> ids) {
        //删除订单表中的订单数据
        for (Integer id : ids) {
            adminOrderMapper.deleteById(id);
        }
    }
}
