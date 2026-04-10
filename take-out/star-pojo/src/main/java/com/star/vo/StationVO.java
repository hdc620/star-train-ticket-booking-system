package com.star.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationVO implements Serializable {
    private Long stationId;
    private String stationCode;
    private String planetName;
    private String galaxyName;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
