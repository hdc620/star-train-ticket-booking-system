package com.star.controller.user;

import com.star.dto.UserOrderPageQueryDTO;
import com.star.result.PageResult;
import com.star.result.Result;
import com.star.service.UserOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/order")
@Slf4j
@Tag(name = "用户查看订单相关接口", description = "")
public class UserOrderController {
    @Autowired
    private UserOrderService userOrderService;

    /**
     * 用户站点分页查询
     *
     * @param userOrderPageQueryDTO
     * @return
     */
    @GetMapping("/page/order")
    @Operation(summary = "用户订单分页查询")
    public Result<PageResult> page(UserOrderPageQueryDTO userOrderPageQueryDTO) {
        log.info("用户订单分页查询:{}", userOrderPageQueryDTO);
        PageResult pageResult = userOrderService.pageQuery(userOrderPageQueryDTO);
        return Result.success(pageResult);
    }
}
