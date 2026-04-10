package com.star.service;

import com.star.dto.UserOrderPageQueryDTO;
import com.star.result.PageResult;

public interface UserOrderService {
    PageResult pageQuery(UserOrderPageQueryDTO userOrderPageQueryDTO);
}
