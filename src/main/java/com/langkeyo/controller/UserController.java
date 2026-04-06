package com.langkeyo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.dto.LoginResponseDTO;
import com.langkeyo.dto.UserDTO;
import com.langkeyo.entity.User;
import com.langkeyo.service.IUserService;
import com.langkeyo.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 * @author langkeyo
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 微信小程序登录
     * @param code 小程序端调用wx.login()获取的code
     * @return token和用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@RequestParam String code) {
        try {
            if (!StringUtils.hasText(code)) {
                return Result.error(ResultCode.PARAM_IS_BLANK);
            }

            LoginResponseDTO result = userService.login(code);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/dev-login")
    public Result<LoginResponseDTO> devLogin(@RequestParam(defaultValue = "1") Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getOpenid());

        LoginResponseDTO result = new LoginResponseDTO();
        UserDTO userDTO = BeanUtil.toBean(user, UserDTO.class);
        result.setToken(token);
        result.setUserInfo(userDTO);

        return Result.success("登录成功", result);
    }

    /**
     * 获取当前用户信息
     * @param token JWT token (从请求头获取)
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserDTO> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            // 去掉 "Bearer " 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            UserDTO userDTO = userService.getUserByToken(token);
            if (userDTO == null) {
                return Result.error(ResultCode.USER_NOT_FOUND);
            }

            return Result.success(userDTO);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(ResultCode.TOKEN_INVALID);
        }
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("后端接口正常运行！");
    }

    @GetMapping("/list")
    public Result<List<User>> getUserList() {
        List<User> list = userService.list();
        return Result.success(list);
    }

    @PutMapping("/leader/{id}")
    public Result<String> updateLeader(@PathVariable Long id, @RequestParam Boolean isLeader) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        user.setIsLeader(Boolean.TRUE.equals(isLeader));
        boolean ok = userService.updateById(user);
        if (!ok) {
            return Result.error("团长状态更新失败");
        }
        return Result.success("团长状态更新成功");
    }
}
