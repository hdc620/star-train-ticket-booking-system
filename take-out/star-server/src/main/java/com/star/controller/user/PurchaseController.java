package com.star.controller.user;

import com.star.dto.PurchaseDTO;
import com.star.dto.ScheduleDTO;
import com.star.result.Result;
import com.star.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/purchase")
@Slf4j
@Tag(name = "用户购票相关接口", description = "")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 购票
     *
     * @param purchaseDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "购票")
    public Result save(@RequestBody PurchaseDTO purchaseDTO) {
        log.info("购票：{}", purchaseDTO);
        purchaseService.save(purchaseDTO);

        return Result.success();
    }
}
