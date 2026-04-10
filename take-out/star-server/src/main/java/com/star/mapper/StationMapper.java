package com.star.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.star.entity.Station;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站点Mapper接口
 */
@Mapper
public interface StationMapper extends BaseMapper<Station> {
}
