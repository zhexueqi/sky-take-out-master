package com.sky.service;


import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * @author zhexueqi
 * @ClassName UserService
 * @since 2024/2/27    9:47
 */
public interface UserService {

    User wxLogin(UserLoginDTO userLoginDTO);
}
