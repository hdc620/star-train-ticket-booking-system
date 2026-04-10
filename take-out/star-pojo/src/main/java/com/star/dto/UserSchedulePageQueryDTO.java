package com.star.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Data
public class UserSchedulePageQueryDTO {
    private Integer page = 1;

    private Integer pageSize = 10;

    // 【核心入参】出发站点主键ID（前端直接传）
    private Long departureStationId;

    // 【核心入参】到达站点主键ID（前端直接传）
    private Long arrivalStationId;

    // 保留原有字段，兼容旧代码
    private String departureStationName;
    private String arrivalStationName;

    // 时间范围格式化
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
