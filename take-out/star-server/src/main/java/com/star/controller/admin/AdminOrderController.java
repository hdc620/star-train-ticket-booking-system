package com.star.controller.admin;

import com.star.dto.AdminOrderDTO;
import com.star.dto.AdminOrderPageQueryDTO;
import com.star.mapper.AdminOrderMapper;
import com.star.result.PageResult;
import com.star.result.Result;
import com.star.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/order")
@Slf4j
@Tag(name = "订单管理相关接口", description = "")
public class AdminOrderController {
    @Autowired
    private AdminOrderService adminOrderService;
    @Autowired
    private AdminOrderMapper adminOrderMapper;

    /**
     * 订单分页查询
     *
     * @param adminOrderPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "订单分页查询")
    public Result<PageResult> page(AdminOrderPageQueryDTO adminOrderPageQueryDTO) {
        log.info("订单分页查询:{}", adminOrderPageQueryDTO);
        PageResult pageResult = adminOrderService.pageQuery(adminOrderPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 修改订单信息
     *
     * @param adminOrderDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "修改订单")
    public Result update(@RequestBody AdminOrderDTO adminOrderDTO) {
        log.info("修改订单：{}", adminOrderDTO);
        adminOrderService.update(adminOrderDTO);

        return Result.success();
    }

    /**
     * 批量删除订单
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @Operation(summary = "批量删除订单")
    public Result delete(@RequestParam List<Integer> ids) {
        log.info("订单批量删除：{}", ids);
        adminOrderService.deleteBatch(ids);

        return Result.success();
    }
}
