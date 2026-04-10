package com.star.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderDTO {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long scheduleId;
    private Integer ticketCount;
    private BigDecimal totalAmount;
    private String status;
}
