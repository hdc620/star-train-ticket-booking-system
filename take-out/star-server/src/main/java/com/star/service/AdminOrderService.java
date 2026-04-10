package com.star.service;

import com.star.dto.AdminOrderDTO;
import com.star.dto.AdminOrderPageQueryDTO;
import com.star.result.PageResult;

import java.util.List;

public interface AdminOrderService {
    PageResult pageQuery(AdminOrderPageQueryDTO adminOrderPageQueryDTO);

    void update(AdminOrderDTO adminOrderDTO);

    void deleteBatch(List<Integer> ids);
}
