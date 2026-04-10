package com.star.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.star.dto.UserDTO;
import com.star.dto.UserLoginDTO;
import com.star.dto.UserUpdatePasswordDTO;
import com.star.entity.User;

public interface UserService {
    /**
     * 注册用户
     * @param userDTO
     */
    void register(UserDTO userDTO);

    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);

    /**
     * 修改用户密码
     * @param userUpdatePasswordDTO
     */
    void updatePassword(Long currentId, UserUpdatePasswordDTO userUpdatePasswordDTO);
}
