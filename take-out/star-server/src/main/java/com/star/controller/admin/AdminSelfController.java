package com.star.controller.admin;

import com.star.constant.JwtClaimsConstant;
import com.star.dto.AdminLoginDTO;
import com.star.mapper.AdminSelfMapper;
import com.star.properties.JwtProperties;
import com.star.context.BaseContext;
import com.star.dto.SelfUpdateDTO;
import com.star.dto.UserLoginDTO;
import com.star.dto.UserUpdatePasswordDTO;
import com.star.entity.User;
import com.star.mapper.UserMapper;
import com.star.result.Result;
import com.star.service.AdminSelfService;
import com.star.service.UserService;
import com.star.utils.JwtUtil;
import com.star.vo.AdminLoginVO;
import com.star.vo.AdminVO;
import com.star.vo.UserLoginVO;
import com.star.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/self")
@Slf4j
@Tag(name = "管理员个人相关接口", description = "")
public class AdminSelfController {
    @Autowired
    private AdminSelfService adminSelfService;
    @Autowired
    private AdminSelfMapper adminSelfMapper;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param adminLoginDTO
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "登录管理员")
    public Result<AdminLoginVO> login(@RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("管理员登录：{}", adminLoginDTO);

        User user = adminSelfService.login(adminLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, user.getUserId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        AdminLoginVO adminLoginVO = AdminLoginVO.builder()
                .id(user.getUserId())
                .userName(user.getUserName())
                .token(token)
                .build();

        return Result.success(adminLoginVO);
    }

    /**
     * 查看个人信息
     *
     * @param
     * @return
     */
    @PostMapping("/view")
    @Operation(summary = "查看管理员信息")
    public Result<AdminVO> view() {
        Long userId= BaseContext.getCurrentId();
        User user=adminSelfMapper.selectById(userId);
        AdminVO adminVO=AdminVO.builder()
                .userName(user.getUserName())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .build();
        return Result.success(adminVO);
    }

    /**
     * 修改管理员信息
     *
     * @param
     * @return
     */
    @PostMapping("/update")
    @Operation(summary = "修改管理员信息")
    public Result update(@RequestBody SelfUpdateDTO selfUpdateDTO) {
        Long userId = BaseContext.getCurrentId();
        // 1. 查询现有管理员信息
        User existingUser = adminSelfMapper.selectById(userId);
        if (existingUser == null) {
            return Result.error("管理员不存在");
        }
        // 2. 仅更新非空字段（避免将 null 覆盖到数据库）
        if (selfUpdateDTO.getUserName() != null) {
            existingUser.setUserName(selfUpdateDTO.getUserName());
        }
        if (selfUpdateDTO.getRealName() != null) {
            existingUser.setRealName(selfUpdateDTO.getRealName());
        }
        if (selfUpdateDTO.getPhone() != null) {
            existingUser.setPhone(selfUpdateDTO.getPhone());
        }
        // 3. 执行更新
        adminSelfMapper.updateById(existingUser);
        return Result.success();
    }

    /**
     * 修改管理员密码
     * @param userUpdatePasswordDTO
     * @return
     */
    @PutMapping("/password")
    @Operation(summary = "修改管理员密码")
    public Result updatePassword(@RequestBody UserUpdatePasswordDTO userUpdatePasswordDTO) {
        // 1. 校验新密码和确认密码是否一致（基础校验）
        if (!userUpdatePasswordDTO.getNewPassword().equals(userUpdatePasswordDTO.getConfirmNewPassword())) {
            return Result.error("新密码与确认密码不一致");
        }
        // 2. 调用服务层处理（获取当前登录用户 + 验证旧密码 + 修改新密码）
        adminSelfService.updatePassword(BaseContext.getCurrentId(), userUpdatePasswordDTO);
        return Result.success("密码修改成功");
    }
}
