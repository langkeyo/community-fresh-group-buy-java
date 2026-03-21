package com.langkeyo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langkeyo.dto.LoginResponseDTO;
import com.langkeyo.dto.UserDTO;
import com.langkeyo.entity.User;
import com.langkeyo.mapper.UserMapper;
import com.langkeyo.service.IUserService;
import com.langkeyo.utils.JwtUtil;
import com.langkeyo.utils.WechatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户业务实现类
 *
 * @author langkeyo
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private WechatUtil wechatUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginResponseDTO login(String code) {
        // 1. 调用微信接口获取openid
        String openid = wechatUtil.getOpenid(code);
        log.info("用户登录，openid: {}", openid);

        // 2. 根据openid查询用户是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getOpenid, openid);
        User user = this.getOne(queryWrapper);

        // 3. 如果用户不存在，自动注册
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("用户" + System.currentTimeMillis() % 10000);
            user.setAvatar("https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132");
            user.setIsLeader(false);
            this.save(user);
            log.info("新用户注册成功，userId: {}", user.getId());
        }

        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getOpenid());

        // 存到 redis
        redisTemplate.opsForValue().set("TOKEN:" + user.getId(), token, 7, TimeUnit.DAYS);

        // 5. 返回用户信息和token
        LoginResponseDTO result = new LoginResponseDTO();

        // 6. 封装数据
        UserDTO userDTO = BeanUtil.toBean(user, UserDTO.class);
        result.setToken(token);
        result.setUserInfo(userDTO);
        System.out.println(userDTO);

        // 7. 返回结果
        return result;
    }

    @Override
    public UserDTO getUserByToken(String token) {
        // 从token中获取userId
        Long userId = jwtUtil.getUserIdFromToken(token);
        // 根据userId查询用户信息
        User user = this.getById(userId);
        return BeanUtil.toBean(user, UserDTO.class);
    }
}