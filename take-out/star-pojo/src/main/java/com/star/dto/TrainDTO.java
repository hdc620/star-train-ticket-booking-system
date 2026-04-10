package com.star.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainDTO {
    private Long trainId;
    private String trainCode;
    private Long totalSeats;
    private String status;
}
