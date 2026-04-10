package com.star.dto;

import lombok.Data;

@Data
public class TrainPageQueryDTO {
    // 页码（默认第1页）
    private Integer page = 1;
    // 每页记录数（默认10条）
    private Integer pageSize = 10;

    private String trainCode;
}
