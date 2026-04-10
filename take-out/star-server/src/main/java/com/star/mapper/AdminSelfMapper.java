package com.star.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.star.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminSelfMapper extends BaseMapper<User> {
}
