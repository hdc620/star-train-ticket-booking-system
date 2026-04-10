package com.star.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 修改密码 DTO（用户自主修改）
 */
@Data // 用Lombok简化getter/setter，无Lombok则手动写
@Schema(description = "用户修改密码 DTO")
public class UserUpdatePasswordDTO {
    @Schema(description = "旧密码")
    private String oldPassword;

    @Schema(description = "新密码")
    private String newPassword;

    @Schema(description = "确认新密码")
    private String confirmNewPassword;
}
