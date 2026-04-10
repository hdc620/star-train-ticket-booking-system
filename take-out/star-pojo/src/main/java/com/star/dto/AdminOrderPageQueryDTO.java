package com.star.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOrderPageQueryDTO {
    // 页码（默认第1页）
    private Integer page = 1;
    // 每页记录数（默认10条）
    private Integer pageSize = 10;

    private String userName;
    private Long scheduleId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
