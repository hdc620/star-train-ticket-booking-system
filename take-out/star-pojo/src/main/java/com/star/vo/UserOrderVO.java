package com.star.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户查询订单返回的数据格式")
public class UserOrderVO {
    private Long orderId;
    private String orderNo;
    private Long scheduleId;
    private String trainCode;
    private Long ticketCount;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createTime;
}
