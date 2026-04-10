package com.star.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.constant.MessageConstant;
import com.star.constant.StatusConstant;
import com.star.dto.ScheduleDTO;
import com.star.dto.SchedulePageQueryDTO;
import com.star.entity.Schedule;
import com.star.entity.Train;
import com.star.exception.ArrivalStationDisableException;
import com.star.exception.BaseException;
import com.star.exception.DepatureStationDisableException;
import com.star.exception.TrainDisableException;
import com.star.mapper.ScheduleMapper;
import com.star.mapper.StationMapper;
import com.star.mapper.TrainMapper;
import com.star.result.PageResult;
import com.star.service.ScheduleService;
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
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private TrainMapper trainMapper;
    @Autowired
    private StationMapper stationMapper;

    /**
     * 新增车次
     */
    @Transactional
    public void save(ScheduleDTO scheduleDTO) {
        //判断出发时间是否早于到达时间
        LocalDateTime departureTime = scheduleDTO.getDepartureTime();
        LocalDateTime arrivalTime = scheduleDTO.getArrivalTime();
        if (!departureTime.isBefore(arrivalTime)) {
            throw new BaseException("出发时间晚于到达时间，新增车次失败");
        }

        //判断出发站和到达站是否启用
        Long depatureStationId = scheduleDTO.getDepartureStationId();
        String depatureStationStatus=stationMapper.selectById(depatureStationId).getStatus();
        if(depatureStationStatus== StatusConstant.STATION_DISABLE){
            throw new DepatureStationDisableException(MessageConstant.DEPATURE_STATION_DISABLE);
        }

        Long arrivalStationId = scheduleDTO.getArrivalStationId();
        String arrivalStationStatus=stationMapper.selectById(arrivalStationId).getStatus();
        if(arrivalStationStatus== StatusConstant.STATION_DISABLE){
            throw new ArrivalStationDisableException(MessageConstant.ARRIVAL_STATION_DISABLE);
        }

        //判断列车是否可用
        Long trainId = scheduleDTO.getTrainId();
        String trainStatus=trainMapper.selectById(trainId).getStatus();
        if(trainStatus== StatusConstant.TRAIN_OUT_OF_SERVICE){
            throw new TrainDisableException(MessageConstant.TRAIN_OUT_OF_SERVICE);
        }
        if(trainStatus== StatusConstant.TRAIN_UNDER_MAINTENANCE){
            throw new TrainDisableException(MessageConstant.TRAIN_UNDER_MAINTENANCE);
        }

        //判断列车安排时间是否冲突
        LambdaQueryWrapper<Schedule> queryWrapperTime = new LambdaQueryWrapper<>();
        queryWrapperTime.eq(Schedule::getTrainId, trainId)
                .between(Schedule::getDepartureTime, departureTime, arrivalTime)
                .between(Schedule::getArrivalTime, arrivalTime, departureTime);

        if (scheduleMapper.exists(queryWrapperTime)) {
            throw new BaseException("列车安排时间冲突，新增车次失败");
        }

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleDTO, schedule);

        // 1. 构建查询条件：车次id
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getScheduleId, schedule.getScheduleId());    // 匹配列车编号

        // 2. 判断是否存在（推荐用 exists，性能远高于 count）
        if (scheduleMapper.exists(queryWrapper)) {
            throw new IllegalArgumentException("车次已存在");
        }

        Long remainingSeats = trainMapper.selectById(trainId).getTotalSeats();
        schedule.setRemainingSeats(remainingSeats);
        // 3. 插入数据库
        scheduleMapper.insert(schedule);
    }

    /**
     * 车次分页查询（
     * @param queryDTO 分页
     * @return 分页结果
     */
    public PageResult pageQuery(SchedulePageQueryDTO queryDTO) {
        // 1. 创建分页对象
        Page<Schedule> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 2. 构建条件构造器
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();

        Long trainId = queryDTO.getTrainId();

        if (trainId != null && !trainId.equals(0L)) {
            queryWrapper.eq(Schedule::getTrainId, trainId);
        }

        Long departureStationId = queryDTO.getDepartureStationId();
        if (departureStationId != null && !departureStationId.equals(0L)) {
            queryWrapper.eq(Schedule::getDepartureStationId, departureStationId);
        }
        Long arrivalStationId = queryDTO.getArrivalStationId();
        if (arrivalStationId != null && !arrivalStationId.equals(0L)) {
            queryWrapper.eq(Schedule::getArrivalStationId, arrivalStationId);
        }
        // 6. 执行分页查询（使用BaseMapper的selectPage方法）
        Page<Schedule> resultPage = scheduleMapper.selectPage(page, queryWrapper);

        // 7. 转换为VO并封装分页结果
        // 1. 批量提取所有 trainId，避免循环查库
        List<Long> trainIds = resultPage.getRecords().stream()
                .map(Schedule::getTrainId)
                .distinct()
                .collect(Collectors.toList());

// 2. 批量查询 Train 并转为 Map（仅需 1 次 SQL）
        Map<Long, Train> trainMap = trainIds.isEmpty()
                ? Collections.emptyMap()
                : trainMapper.selectBatchIds(trainIds).stream()
                .collect(Collectors.toMap(Train::getTrainId, Function.identity()));

// 3. 转换 VO 并处理异常情况
        List<ScheduleVO> voList = resultPage.getRecords().stream()
                .map(schedule -> {
                    ScheduleVO vo = new ScheduleVO();
                    BeanUtils.copyProperties(schedule, vo);

                    // 安全获取 Train 对象
                    Train train = trainMap.get(schedule.getTrainId());
                    if (train != null && train.getTotalSeats() > 0) {
                        BigDecimal totalSeats = BigDecimal.valueOf(train.getTotalSeats());
                        BigDecimal remaining = BigDecimal.valueOf(schedule.getRemainingSeats());
                        // 计算百分比（保留 2 位小数，四舍五入）
                        BigDecimal seatsPercent = remaining.divide(totalSeats, 2, RoundingMode.HALF_UP);
                        vo.setSeatsPercent(seatsPercent);
                    } else {
                        // 异常情况默认 0%，避免崩溃
                        vo.setSeatsPercent(BigDecimal.ZERO);
                    }
                    String trainCode=train.getTrainCode();
                    vo.setTrainCode(trainCode);
                    String departureStationName=stationMapper.selectById(schedule.getDepartureStationId()).getPlanetName();
                    vo.setDepartureStationName(departureStationName);
                    String arrivalStationName=stationMapper.selectById(schedule.getArrivalStationId()).getPlanetName();
                    vo.setArrivalStationName(arrivalStationName);
                    return vo;
                })
                .collect(Collectors.toList());

        PageResult pageResult = new PageResult();
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setRecords(voList);

        return pageResult;
    }

    /**
     * 车次批量删除
     */
    @Transactional
    public void deleteBatch(List<Integer> ids) {
        //删除车次表中的车次数据
        for (Integer id : ids) {
            scheduleMapper.deleteById(id);
        }
    }

    /**
     * 修改车次基本信息
     */
    @Transactional
    public void update(ScheduleDTO scheduleDTO) {
        //判断出发时间是否早于到达时间
        LocalDateTime departureTime = scheduleDTO.getDepartureTime();
        LocalDateTime arrivalTime = scheduleDTO.getArrivalTime();
        if (!departureTime.isBefore(arrivalTime)) {
            throw new BaseException("出发时间晚于到达时间，新增车次失败");
        }

        //判断出发站和到达站是否启用
        Long depatureStationId = scheduleDTO.getDepartureStationId();
        String depatureStationStatus=stationMapper.selectById(depatureStationId).getStatus();
        if(depatureStationStatus== StatusConstant.STATION_DISABLE){
            throw new DepatureStationDisableException(MessageConstant.DEPATURE_STATION_DISABLE);
        }

        Long arrivalStationId = scheduleDTO.getArrivalStationId();
        String arrivalStationStatus=stationMapper.selectById(arrivalStationId).getStatus();
        if(arrivalStationStatus== StatusConstant.STATION_DISABLE){
            throw new ArrivalStationDisableException(MessageConstant.ARRIVAL_STATION_DISABLE);
        }

        //判断列车是否可用
        Long trainId = scheduleDTO.getTrainId();
        String trainStatus=trainMapper.selectById(trainId).getStatus();
        if(trainStatus== StatusConstant.TRAIN_OUT_OF_SERVICE){
            throw new TrainDisableException(MessageConstant.TRAIN_OUT_OF_SERVICE);
        }
        if(trainStatus== StatusConstant.TRAIN_UNDER_MAINTENANCE){
            throw new TrainDisableException(MessageConstant.TRAIN_UNDER_MAINTENANCE);
        }

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleDTO, schedule);

        //修改车次表基本信息
        scheduleMapper.updateById(schedule);
    }
}
