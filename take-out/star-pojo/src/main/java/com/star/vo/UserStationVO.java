package com.star.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户查询启用站点返回的数据格式")
public class UserStationVO {
    // 【核心新增】站点主键ID，前端直接用这个传参
    @Schema(description = "站点主键ID")
    private Long stationId;

    @Schema(description = "站点编码")
    private String stationCode;

    @Schema(description = "星球名称")
    private String planetName;

    @Schema(description = "星系名称")
    private String galaxyName;
}
