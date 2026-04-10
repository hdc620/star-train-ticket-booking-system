package com.star.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "修改个人信息时传递的数据模型")
public class SelfUpdateDTO {
    @Schema(description = "账号")
    private String userName;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;
}
