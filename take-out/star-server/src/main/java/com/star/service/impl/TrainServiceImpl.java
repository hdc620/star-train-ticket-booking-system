package com.star.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.constant.StatusConstant;
import com.star.dto.TrainDTO;
import com.star.dto.TrainPageQueryDTO;
import com.star.entity.Schedule;
import com.star.entity.Train;
import com.star.exception.BaseException;
import com.star.mapper.ScheduleMapper;
import com.star.mapper.TrainMapper;
import com.star.result.PageResult;
import com.star.result.Result;
import com.star.service.TrainService;
import com.star.vo.TrainVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TrainServiceImpl extends ServiceImpl<TrainMapper, Train> implements TrainService {
    @Autowired
    private TrainMapper trainMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * 新增列车
     */
    @Transactional
    public void save(TrainDTO trainDTO) {
        Train train = new Train();
        BeanUtils.copyProperties(trainDTO, train);

        // 1. 构建查询条件： 列车编号
        LambdaQueryWrapper<Train> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Train::getTrainCode, train.getTrainCode());    // 匹配列车编号

        // 2. 判断是否存在（推荐用 exists，性能远高于 count）
        if (trainMapper.exists(queryWrapper)) {
            throw new IllegalArgumentException("站点名称已存在");
        }

        // 3. 插入数据库
        trainMapper.insert(train);
    }

    /**
     * 站点分页查询（
     * @param queryDTO 分页
     * @return 分页结果
     */
    public PageResult pageQuery(TrainPageQueryDTO queryDTO) {
        // 1. 创建分页对象
        Page<Train> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        // 2. 构建条件构造器
        LambdaQueryWrapper<Train> queryWrapper = new LambdaQueryWrapper<>();

        String trainCode = queryDTO.getTrainCode();

        if (trainCode != null && !trainCode.isEmpty()) {
            queryWrapper.eq(Train::getTrainCode, trainCode);
        }

        // 6. 执行分页查询（使用BaseMapper的selectPage方法）
        Page<Train> resultPage = trainMapper.selectPage(page, queryWrapper);

        // 7. 转换为VO并封装分页结果
        List<TrainVO> voList = resultPage.getRecords().stream()
                .map(train -> {
                    TrainVO vo = new TrainVO();
                    BeanUtils.copyProperties(train, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        PageResult pageResult = new PageResult();
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setRecords(voList);

        return pageResult;
    }

    /**
     * 列车批量删除
     */
    @Transactional
    public void deleteBatch(List<Integer> ids) {
        //删除列车表中的列车数据
        for (Integer id : ids) {
            trainMapper.deleteById(id);
        }
    }

    /**
     * 修改列车基本信息
     */
    @Transactional
    public Result update(TrainDTO trainDTO) {
        Long trainId = trainDTO.getTrainId();
        Train existingTrain = trainMapper.selectById(trainId);
        if (existingTrain == null) {
            throw new BaseException("列车不存在");
        }

        // 1. 先准备好要更新的对象
        Train trainToUpdate = new Train();
        BeanUtils.copyProperties(trainDTO, trainToUpdate);

        // 2. 【核心逻辑】判断是否需要拦截状态变更
        String currentStatus = existingTrain.getStatus();
        String targetStatus = trainDTO.getStatus();

        boolean isStatusChangeNeedCheck =
                StatusConstant.TRAIN_IN_OPERATION.equals(currentStatus) &&
                        (StatusConstant.TRAIN_OUT_OF_SERVICE.equals(targetStatus) ||
                                StatusConstant.TRAIN_UNDER_MAINTENANCE.equals(targetStatus));

        String warnMessage = null;
        if (isStatusChangeNeedCheck) {
            LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(Schedule::getTrainId, trainId)
                    .eq(Schedule::getStatus, StatusConstant.SCHEDULE_AVAILABLE)
                    .last("LIMIT 1");

            if (scheduleMapper.selectCount(queryWrapper) > 0) {
                trainToUpdate.setStatus(currentStatus);
                warnMessage = "列车 " + trainDTO.getTrainCode() + " 因车次售票中，状态未修改";
                log.warn(warnMessage);
            }
        }

        // 4. 【统一更新】不管状态有没有被改回，其他字段都会正常更新
        trainMapper.updateById(trainToUpdate);

        // 返回 Result，带提示信息
        if (warnMessage != null) {
            return Result.success(warnMessage); // 或者用 Result.error，但这是部分成功，看你业务定义
        }
        return Result.success();
    }
}
