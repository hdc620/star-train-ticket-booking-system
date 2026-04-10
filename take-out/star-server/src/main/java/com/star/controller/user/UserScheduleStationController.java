package com.star.controller.user;

import com.star.dto.UserSchedulePageQueryDTO;
import com.star.dto.UserStationPageQueryDTO;
import com.star.result.PageResult;
import com.star.result.Result;
import com.star.service.UserScheduleStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户车次查询相关接口", description = "")
public class UserScheduleStationController {
    @Autowired
    private UserScheduleStationService userScheduleStationService;

    /**
     * 用户站点分页查询
     *
     * @param userStationPageQueryDTO
     * @return
     */
    @GetMapping("/page/station")
    @Operation(summary = "用户站点分页查询")
    public Result<PageResult> pageStation(UserStationPageQueryDTO userStationPageQueryDTO) {
        log.info("用户站点分页查询:{}", userStationPageQueryDTO);
        PageResult pageResult = userScheduleStationService.pageQueryStation(userStationPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 用户车次分页查询
     *
     * @param userSchedulePageQueryDTO
     * @return
     */
    @GetMapping("/page/schedule")
    @Operation(summary = "用户车次分页查询")
    public Result<PageResult> page(UserSchedulePageQueryDTO userSchedulePageQueryDTO) {
        log.info("用户车次分页查询:{}", userSchedulePageQueryDTO);
        PageResult pageResult = userScheduleStationService.pageQuerySchedule(userSchedulePageQueryDTO);
        return Result.success(pageResult);
    }
}
