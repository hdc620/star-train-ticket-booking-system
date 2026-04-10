package com.star.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

@Data
@TableName("t_schedule")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long scheduleId;
    private Long trainId;
    private Long departureStationId;
    private Long arrivalStationId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal ticketPrice;
    private Long remainingSeats;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
