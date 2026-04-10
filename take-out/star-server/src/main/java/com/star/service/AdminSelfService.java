package com.star.service;

import com.star.dto.AdminLoginDTO;
import com.star.dto.UserUpdatePasswordDTO;
import com.star.entity.User;

public interface AdminSelfService {
    User login(AdminLoginDTO adminLoginDTO);

    void updatePassword(Long currentId, UserUpdatePasswordDTO userUpdatePasswordDTO);
}
