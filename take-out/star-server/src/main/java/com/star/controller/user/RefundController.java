package com.star.controller.user;

import com.star.result.Result;
import com.star.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/refund")
@Slf4j
@Tag(name = "用户退票相关接口", description = "")
public class RefundController {
    @Autowired
    private RefundService refundService;

    /**
     * 退票
     *
     * @param id
     * @return
     */
    @PutMapping
    @Operation(summary = "退票")
    public Result refund(@RequestParam Long id) {
        log.info("退票：{}", id);
        refundService.refund(id);

        return Result.success();
    }
}
