package com.star.service;

import com.star.dto.UserSchedulePageQueryDTO;
import com.star.dto.UserStationPageQueryDTO;
import com.star.result.PageResult;

public interface UserScheduleStationService {
    PageResult pageQueryStation(UserStationPageQueryDTO userStationPageQueryDTO);

    PageResult pageQuerySchedule(UserSchedulePageQueryDTO userSchedulePageQueryDTO);
}
