package com.star.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.star.constant.MessageConstant;
import com.star.dto.UserDTO;
import com.star.dto.UserLoginDTO;
import com.star.dto.UserUpdatePasswordDTO;
import com.star.entity.User;
import com.star.exception.PasswordErrorException;
import com.star.mapper.UserMapper;
import com.star.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import com.star.exception.AccountNotFoundException;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserName, username));

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (user == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //对前端传过来的明文密码进行md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        //3、返回实体对象
        return user;
    }

    /**
     * 注册用户
     *
     * @param userDTO
     */
    public void register(UserDTO userDTO) {
        User user = new User();

        //对象属性拷贝
        BeanUtils.copyProperties(userDTO, user);

        //密码加密处理
        String password = DigestUtils.md5DigestAsHex(userDTO.getPassword().getBytes());
        user.setPassword(password);

        userMapper.insert(user);
    }

    /**
     * 修改用户密码
     *
     * @param id
     * @param userUpdatePasswordDTO
     */
    public void updatePassword(Long id, UserUpdatePasswordDTO userUpdatePasswordDTO) {
        // 1. 查询用户
        User user = userMapper.selectById(id);
        if(user==null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 验证旧密码
        String oldPassword = userUpdatePasswordDTO.getOldPassword();
        String encryptedOldPwd = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!encryptedOldPwd.equals(user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        // 3. 加密新密码并更新
        String encryptedNewPwd = DigestUtils.md5DigestAsHex(userUpdatePasswordDTO.getNewPassword().getBytes());
        user.setPassword(encryptedNewPwd);
        userMapper.updateById(user);
    }
}
