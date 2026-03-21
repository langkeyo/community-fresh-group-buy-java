package com.langkeyo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.langkeyo.dto.LoginResponseDTO;
import com.langkeyo.dto.UserDTO;
import com.langkeyo.entity.User;

import java.util.Map;

/**
 * 用户业务接口
 * @author langkeyo
 */
public interface IUserService extends IService<User> {

    /**
     * 微信小程序登录
     * @param code 小程序端获取的code
     * @return token和用户信息
     */
    LoginResponseDTO login(String code);

    /**
     * 根据token获取用户信息
     * @param token JWT token
     * @return 用户信息
     */
    UserDTO getUserByToken(String token);
}