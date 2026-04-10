package com.star.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.star.entity.Order;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface AdminOrderMapper extends BaseMapper<Order> {
}
