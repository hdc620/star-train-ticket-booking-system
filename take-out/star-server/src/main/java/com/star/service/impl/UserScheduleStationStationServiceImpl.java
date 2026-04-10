package com.star.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.constant.StatusConstant;
import com.star.dto.UserSchedulePageQueryDTO;
import com.star.dto.UserStationPageQueryDTO;
import com.star.entity.Schedule;
import com.star.entity.Station;
import com.star.entity.Train;
import com.star.mapper.ScheduleMapper;
import com.star.mapper.StationMapper;
import com.star.mapper.TrainMapper;
import com.star.result.PageResult;
import com.star.service.UserScheduleStationService;
import com.star.vo.UserScheduleVO;
import com.star.vo.UserStationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserScheduleStationStationServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements UserScheduleStationService {
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private StationMapper stationMapper;
    @Autowired
    private TrainMapper trainMapper;

    /**
     * 站点分页查询（优化版）
     */
    @Override
    public PageResult pageQueryStation(UserStationPageQueryDTO queryDTO) {
        // 1. 分页对象构建
        Page<Station> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 2. 查询条件：只查启用的站点
        LambdaQueryWrapper<Station> queryWrapper = new LambdaQueryWrapper<Station>()
                .eq(Station::getStatus, StatusConstant.STATION_ENABLE);

        // 3. 执行分页查询
        Page<Station> resultPage = stationMapper.selectPage(page, queryWrapper);

        // 4. 实体转VO
        List<UserStationVO> voList = CollectionUtils.isEmpty(resultPage.getRecords())
                ? Collections.emptyList()
                : resultPage.getRecords().stream()
                .map(station -> {
                    UserStationVO vo = new UserStationVO();
                    BeanUtils.copyProperties(station, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        // 5. 封装分页结果
        return PageResult.builder()
                .total(resultPage.getTotal())
                .records(voList)
                .build();
    }

    /**
     * 车次分页查询（最优版）
     */
    @Override
    public PageResult pageQuerySchedule(UserSchedulePageQueryDTO queryDTO) {
        // 1. 分页对象构建
        Page<Schedule> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 2. 构建查询条件（Lambda链式写法，更简洁）
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<Schedule>()
                // 核心条件：只查可售票的车次
                .eq(Schedule::getStatus, StatusConstant.SCHEDULE_AVAILABLE)
                // 出发站点ID（主键查询，索引命中）
                .eq(queryDTO.getDepartureStationId() != null, Schedule::getDepartureStationId, queryDTO.getDepartureStationId())
                // 到达站点ID（主键查询，索引命中）
                .eq(queryDTO.getArrivalStationId() != null, Schedule::getArrivalStationId, queryDTO.getArrivalStationId())
                // 出发时间范围
                .ge(queryDTO.getStartTime() != null, Schedule::getDepartureTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, Schedule::getDepartureTime, queryDTO.getEndTime());

        // 3. 执行分页查询（仅1次数据库查询）
        Page<Schedule> resultPage = scheduleMapper.selectPage(page, queryWrapper);
        List<Schedule> scheduleList = resultPage.getRecords();

        // 4. 空值快速返回，避免后续无效逻辑
        if (CollectionUtils.isEmpty(scheduleList)) {
            return PageResult.builder()
                    .total(resultPage.getTotal())
                    .records(Collections.emptyList())
                    .build();
        }

        // ------------------------------
        // 【核心性能优化】批量查询关联数据，杜绝循环内查库
        // ------------------------------
        // 4.1 提取所有关联ID，去重
        Set<Long> trainIdSet = scheduleList.stream().map(Schedule::getTrainId).collect(Collectors.toSet());
        Set<Long> stationIdSet = scheduleList.stream()
                .flatMap(s -> List.of(s.getDepartureStationId(), s.getArrivalStationId()).stream())
                .collect(Collectors.toSet());

        // 4.2 批量查询（仅2次数据库查询，无论多少条数据）
        List<Train> trainList = trainIdSet.isEmpty() ? Collections.emptyList() : trainMapper.selectBatchIds(trainIdSet);
        List<Station> stationList = stationIdSet.isEmpty() ? Collections.emptyList() : stationMapper.selectBatchIds(stationIdSet);

        // 4.3 转Map，O(1)查找，循环内无需查库
        Map<Long, String> trainCodeMap = trainList.stream()
                .collect(Collectors.toMap(Train::getTrainId, Train::getTrainCode, (k1, k2) -> k1));
        Map<Long, String> stationPlanetNameMap = stationList.stream()
                .collect(Collectors.toMap(Station::getStationId, Station::getPlanetName, (k1, k2) -> k1));

        // 5. 批量组装VO（内存操作，无数据库IO）
        List<UserScheduleVO> voList = scheduleList.stream()
                .map(schedule -> {
                    UserScheduleVO vo = new UserScheduleVO();
                    BeanUtils.copyProperties(schedule, vo);
                    // 从Map中快速获取关联数据
                    vo.setTrainCode(trainCodeMap.getOrDefault(schedule.getTrainId(), "未知"));
                    vo.setDepartureStationName(stationPlanetNameMap.getOrDefault(schedule.getDepartureStationId(), "未知"));
                    vo.setArrivalStationName(stationPlanetNameMap.getOrDefault(schedule.getArrivalStationId(), "未知"));
                    return vo;
                })
                .collect(Collectors.toList());

        // 6. 封装返回结果
        return PageResult.builder()
                .total(resultPage.getTotal())
                .records(voList)
                .build();
    }
}
