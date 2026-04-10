package com.star.controller.admin;

import com.star.dto.StationDTO;
import com.star.dto.TrainDTO;
import com.star.dto.TrainPageQueryDTO;
import com.star.entity.Train;
import com.star.mapper.TrainMapper;
import com.star.result.PageResult;
import com.star.result.Result;
import com.star.service.TrainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/train")
@Slf4j
@Tag(name = "列车管理相关接口", description = "")
public class TrainController {
    @Autowired
    private TrainService trainService;
    @Autowired
    private TrainMapper trainMapper;

    /**
     * 新增列车
     *
     * @param trainDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增列车")
    public Result save(@RequestBody TrainDTO trainDTO) {
        log.info("新增列车：{}", trainDTO);
        trainService.save(trainDTO);

        return Result.success();
    }

    /**
     * 批量删除列车
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @Operation(summary = "批量删除列车")
    public Result delete(@RequestParam List<Integer> ids) {
        log.info("列车批量删除：{}", ids);
        trainService.deleteBatch(ids);

        return Result.success();
    }

    /**
     * 列车分页查询
     *
     * @param trainPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "列车分页查询")
    public Result<PageResult> page(TrainPageQueryDTO trainPageQueryDTO) {
        log.info("列车分页查询:{}", trainPageQueryDTO);
        PageResult pageResult = trainService.pageQuery(trainPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 修改列车信息
     *
     * @param trainDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "修改列车")
    public Result update(@RequestBody TrainDTO trainDTO) {
        log.info("修改列车：{}", trainDTO);
        trainService.update(trainDTO);

        return Result.success();
    }
}
