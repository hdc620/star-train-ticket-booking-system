package com.star.controller.admin;

import com.star.dto.ScheduleDTO;
import com.star.dto.SchedulePageQueryDTO;
import com.star.mapper.ScheduleMapper;
import com.star.result.PageResult;
import com.star.result.Result;
import com.star.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/schedule")
@Slf4j
@Tag(name = "车次管理相关接口", description = "")
public class AdminScheduleController {
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * 新增车次
     *
     * @param scheduleDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增车次")
    public Result save(@RequestBody ScheduleDTO scheduleDTO) {
        log.info("新增车次：{}", scheduleDTO);
        scheduleService.save(scheduleDTO);

        return Result.success();
    }

    /**
     * 批量删除车次
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @Operation(summary = "批量删除车次")
    public Result delete(@RequestParam List<Integer> ids) {
        log.info("车次批量删除：{}", ids);
        scheduleService.deleteBatch(ids);

        return Result.success();
    }

    /**
     * 车次分页查询
     *
     * @param schedulePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "车次分页查询")
    public Result<PageResult> page(SchedulePageQueryDTO schedulePageQueryDTO) {
        log.info("车次分页查询:{}", schedulePageQueryDTO);
        PageResult pageResult = scheduleService.pageQuery(schedulePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 修改车次信息
     *
     * @param scheduleDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "修改车次")
    public Result update(@RequestBody ScheduleDTO scheduleDTO) {
        log.info("修改车次：{}", scheduleDTO);
        scheduleService.update(scheduleDTO);

        return Result.success();
    }
}
