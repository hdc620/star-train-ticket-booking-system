package com.star.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainVO {
    private Long trainId;
    private String trainCode;
    private Long totalSeats;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
