package com.star.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleVO {
    private Long scheduleId;
    private String trainCode;
    private String departureStationName;
    private String arrivalStationName;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal ticketPrice;
    private Long remainingSeats;
    private String status;
    private BigDecimal seatsPercent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
