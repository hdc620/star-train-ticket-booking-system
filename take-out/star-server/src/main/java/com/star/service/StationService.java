package com.star.service;

import com.star.dto.StationDTO;
import com.star.dto.StationPageQueryDTO;
import com.star.result.PageResult;

import java.util.List;

public interface StationService {
    void save(StationDTO stationDTO);

    void deleteBatch(List<Long> ids);

    PageResult pageQuery(StationPageQueryDTO stationPageQueryDTO);

    void update(StationDTO stationDTO);
}
