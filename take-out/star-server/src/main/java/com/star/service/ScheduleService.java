package com.star.service;

import com.star.dto.ScheduleDTO;
import com.star.dto.SchedulePageQueryDTO;
import com.star.result.PageResult;

import java.util.List;

public interface ScheduleService {
    void save(ScheduleDTO scheduleDTO);

    void deleteBatch(List<Integer> ids);

    PageResult pageQuery(SchedulePageQueryDTO schedulePageQueryDTO);

    void update(ScheduleDTO scheduleDTO);
}
