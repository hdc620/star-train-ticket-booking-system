package com.star.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_train")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Train implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long trainId;
    private String trainCode;
    private Long totalSeats;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
