package com.star.controller.admin;

import com.star.dto.StationDTO;
import com.star.dto.StationPageQueryDTO;
import com.star.entity.Station;
import com.star.mapper.StationMapper;
import com.star.mapper.UserMapper;
import com.star.properties.JwtProperties;
import com.star.result.PageResult;
import com.star.result.Result;
import com.star.service.StationService;
import com.star.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.star.dto.StationDTO;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/station")
@Slf4j
@Tag(name = "站点管理相关接口", description = "")
public class StationController {
    @Autowired
    private StationService stationService;
    @Autowired
    private StationMapper stationMapper;

    /**
     * 新增站点
     *
     * @param stationDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增站点")
    public Result save(@RequestBody StationDTO stationDTO) {
        log.info("新增站点：{}", stationDTO);
        stationService.save(stationDTO);

        return Result.success();
    }

    /**
     * 批量删除站点
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @Operation(summary = "批量删除站点")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("站点批量删除：{}", ids);
        stationService.deleteBatch(ids);

        return Result.success();
    }

    /**
     * 站点分页查询
     *
     * @param stationPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "站点分页查询")
    public Result<PageResult> page(StationPageQueryDTO stationPageQueryDTO) {
        log.info("站点分页查询:{}", stationPageQueryDTO);
        PageResult pageResult = stationService.pageQuery(stationPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 修改站点信息
     *
     * @param stationDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "修改站点")
    public Result update(@RequestBody StationDTO stationDTO) {
        log.info("修改站点：{}", stationDTO);
        stationService.update(stationDTO);

        return Result.success();
    }
}
