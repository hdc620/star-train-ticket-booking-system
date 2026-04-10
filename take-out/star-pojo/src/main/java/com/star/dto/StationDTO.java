package com.star.dto;

import lombok.Data;

@Data
public class StationDTO {
    private Long stationId;
    private String stationCode;
    private String planetName;
    private String galaxyName;
    private String status;
}
