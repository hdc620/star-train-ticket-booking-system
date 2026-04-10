package com.star.controller.user;

import com.star.constant.JwtClaimsConstant;
import com.star.context.BaseContext;
import com.star.dto.SelfUpdateDTO;
import com.star.dto.UserDTO;
import com.star.dto.UserLoginDTO;
import com.star.dto.UserUpdatePasswordDTO;
import com.star.mapper.UserMapper;
import com.star.properties.JwtProperties;
import com.star.result.Result;
import com.star.service.UserService;
import com.star.utils.JwtUtil;
import com.star.vo.UserLoginVO;
import com.star.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.star.entity.User;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/self")
@Slf4j
@Tag(name = "用户个人相关接口", description = "")
public class UserSelfController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 注册用户
     * @param userDTO
     * @return
     */
    @PostMapping("/register")
    @Operation(summary = "注册用户")
    public Result register(@RequestBody UserDTO userDTO){
        log.info("注册用户：{}",userDTO);
        userService.register(userDTO);
        return Result.success();
    }

    /**
     * 登录
     *
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "登录用户")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);

        User user = userService.login(userLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getUserId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getUserId())
                .userName(user.getUserName())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }

    /**
     * 查看个人信息
     *
     * @param
     * @return
     */
    @PostMapping("/view")
    @Operation(summary = "查看用户信息")
    public Result<UserVO> view() {
        Long userId=BaseContext.getCurrentId();
        User user=userMapper.selectById(userId);
        UserVO userVO=UserVO.builder()
                .userName(user.getUserName())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .build();
        return Result.success(userVO);
    }

    /**
     * 修改个人信息
     *
     * @param
     * @return
     */
    @PostMapping("/update")
    @Operation(summary = "修改个人信息")
    public Result update(@RequestBody SelfUpdateDTO selfUpdateDTO) {
        User user=new User();
        BeanUtils.copyProperties(selfUpdateDTO,user);
        Long userId=BaseContext.getCurrentId();
        user.setUserId(userId);
        userMapper.updateById(user);
        return Result.success();
    }

    /**
     * 修改用户密码
     * @param userUpdatePasswordDTO
     * @return
     */
    @PutMapping("/password")
    @Operation(summary = "修改用户密码")
    public Result updatePassword(@RequestBody UserUpdatePasswordDTO userUpdatePasswordDTO) {
        // 1. 校验新密码和确认密码是否一致（基础校验）
        if (!userUpdatePasswordDTO.getNewPassword().equals(userUpdatePasswordDTO.getConfirmNewPassword())) {
            return Result.error("新密码与确认密码不一致");
        }
        // 2. 调用服务层处理（获取当前登录用户 + 验证旧密码 + 修改新密码）
        userService.updatePassword(BaseContext.getCurrentId(), userUpdatePasswordDTO);
        return Result.success("密码修改成功");
    }
}
