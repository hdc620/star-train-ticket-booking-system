package com.star.service;

import com.star.dto.StationDTO;
import com.star.dto.TrainDTO;
import com.star.dto.TrainPageQueryDTO;
import com.star.entity.Train;
import com.star.result.PageResult;
import com.star.result.Result;

import java.util.List;

public interface TrainService {
    void save(TrainDTO trainDTO);

    void deleteBatch(List<Integer> ids);

    PageResult pageQuery(TrainPageQueryDTO trainPageQueryDTO);

    Result update(TrainDTO trainDTO);
}
