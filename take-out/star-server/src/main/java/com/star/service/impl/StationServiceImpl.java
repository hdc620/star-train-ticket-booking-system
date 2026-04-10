package com.star.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.constant.MessageConstant;
import com.star.context.BaseContext;
import com.star.dto.StationDTO;
import com.star.dto.StationPageQueryDTO;
import com.star.entity.Schedule;
import com.star.entity.Station;
import com.star.exception.StationArrangedException;
import com.star.mapper.ScheduleMapper;
import com.star.result.PageResult;
import com.star.service.StationService;
import com.star.vo.StationVO;
import io.swagger.v3.oas.models.info.Contact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.star.mapper.StationMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements StationService {
    @Autowired
    private StationMapper stationMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * 新增站点
     */
    @Transactional
    public void save(StationDTO stationDTO) {
        Station station = new Station();
        BeanUtils.copyProperties(stationDTO, station);

        // 1. 构建查询条件： 站点名
        LambdaQueryWrapper<Station> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Station::getPlanetName, station.getPlanetName());    // 匹配站点名

        // 2. 判断是否存在（推荐用 exists，性能远高于 count）
        if (stationMapper.exists(queryWrapper)) {
            throw new IllegalArgumentException("站点名称已存在");
        }

        // 3. 插入数据库
        stationMapper.insert(station);
    }

    /**
     * 站点分页查询（
     * @param queryDTO 分页
     * @return 分页结果
     */
    public PageResult pageQuery(StationPageQueryDTO queryDTO) {
        // 1. 创建分页对象
        Page<Station> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 2. 构建条件构造器
        LambdaQueryWrapper<Station> queryWrapper = new LambdaQueryWrapper<>();

        String planetName = queryDTO.getPlanetName();
        String galaxyName = queryDTO.getGalaxyName();
        if (planetName != null && !planetName.isEmpty()) {
            queryWrapper.eq(Station::getPlanetName, planetName);
        }
        if (galaxyName != null && !galaxyName.isEmpty()) {
            queryWrapper.eq(Station::getGalaxyName, galaxyName);
        }

        // 6. 执行分页查询（使用BaseMapper的selectPage方法）
        Page<Station> resultPage = stationMapper.selectPage(page, queryWrapper);

        // 7. 转换为VO并封装分页结果
        List<StationVO> voList = resultPage.getRecords().stream()
                .map(station -> {
                    StationVO vo = new StationVO();
                    BeanUtils.copyProperties(station, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        PageResult pageResult = new PageResult();
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setRecords(voList);

        return pageResult;
    }

    /**
     * 站点批量删除
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        // 1. 批量查询所有待删除站点
        List<Station> stations = stationMapper.selectBatchIds(ids);
        if (stations.isEmpty()) {
            return;
        }
        Map<Long, Station> stationMap = stations.stream()
                .collect(Collectors.toMap(Station::getStationId, s -> s));

        // 2. 一次性查出所有被班次占用的站点id
        LambdaQueryWrapper<Schedule> scheduleQuery = new LambdaQueryWrapper<>();
        scheduleQuery
                .in(Schedule::getDepartureStationId, ids)
                .or()
                .in(Schedule::getArrivalStationId, ids);

        List<Schedule> relatedSchedules = scheduleMapper.selectList(
                scheduleQuery.select(Schedule::getDepartureStationId, Schedule::getArrivalStationId)
        );

        // 3. 收集所有被占用的站点id
        Set<Long> occupiedIds = new HashSet<>();
        for (Schedule schedule : relatedSchedules) {
            occupiedIds.add(schedule.getDepartureStationId());
            occupiedIds.add(schedule.getArrivalStationId());
        }

        // 4. 分离可删除的id和不可删除的id
        List<Long> canDeleteIds = new ArrayList<>();
        List<String> skipMessages = new ArrayList<>(); // 用于记录日志

        for (Long id : ids) {
            Station station = stationMap.get(id);
            if (station == null) {
                continue;
            }
            if (occupiedIds.contains(id)) {
                // 被占用，记录日志信息，不执行删除
                skipMessages.add(station.getStationCode() + "站点已被安排使用");
            } else {
                // 未被占用，加入待删除列表
                canDeleteIds.add(id);
            }
        }

        // 5. 批量删除所有符合条件的站点
        if (!canDeleteIds.isEmpty()) {
            stationMapper.deleteBatchIds(canDeleteIds);
            log.info("成功删除站点，ids: {}", canDeleteIds);
        }

        // 6. 如果有跳过的，记录日志
        if (!skipMessages.isEmpty()) {
            log.warn("部分站点未删除，原因:{}", skipMessages);
        }
    }

    /**
     * 修改站点基本信息
     */
    @Transactional
    public void update(StationDTO stationDTO) {
        Station station = new Station();
        BeanUtils.copyProperties(stationDTO, station);

        //修改站点表基本信息
        stationMapper.updateById(station);
    }
}
