package com.langkeyo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.dto.LoginResponseDTO;
import com.langkeyo.dto.AdminLoginReqDTO;
import com.langkeyo.dto.UserDTO;
import com.langkeyo.dto.UserManageReqDTO;
import com.langkeyo.entity.User;
import com.langkeyo.service.IUserService;
import com.langkeyo.utils.JwtUtil;
import com.langkeyo.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户控制器
 * @author langkeyo
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1\\d{10}$");

    @Autowired
    private IUserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${app.admin.recovery-key:local-recover-2026}")
    private String adminRecoveryKey;

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
    public Result<LoginResponseDTO> devLogin(@RequestParam(required = false) Long userId,
                                             @RequestParam(required = false) String code) {
        // 兼容管理端当前调用：/api/user/dev-login?code=admin-test-code
        if (userId == null) {
            userId = resolveDevUserId(code);
        }
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

    @PostMapping("/admin-login")
    public Result<LoginResponseDTO> adminLogin(@RequestBody AdminLoginReqDTO req) {
        if (req == null || !StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
            return Result.error("账号或密码不能为空");
        }
        String username = req.getUsername().trim();
        User user = userService.lambdaQuery()
                .eq(User::getLoginName, username)
                .last("limit 1")
                .one();
        if (user == null) {
            return Result.error("账号不存在");
        }
        if (!PasswordUtil.matches(req.getPassword().trim(), user.getPasswordHash())) {
            return Result.error("密码错误");
        }
        if ("leader".equalsIgnoreCase(user.getAdminRole())) {
            return Result.error("该账号无后台管理权限");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getOpenid());
        redisTemplate.opsForValue().set("TOKEN:" + user.getId(), token, 7, java.util.concurrent.TimeUnit.DAYS);
        LoginResponseDTO result = new LoginResponseDTO();
        result.setToken(token);
        result.setUserInfo(BeanUtil.toBean(user, UserDTO.class));
        return Result.success("登录成功", result);
    }

    @PostMapping("/recover-default-admin")
    public Result<String> recoverDefaultAdmin(@RequestParam String recoveryKey) {
        if (!StringUtils.hasText(recoveryKey) || !adminRecoveryKey.equals(recoveryKey.trim())) {
            return Result.error("恢复密钥错误");
        }
        String defaultUsername = "admin";
        String defaultPassword = "Admin@123456";
        User admin = userService.lambdaQuery()
                .eq(User::getLoginName, defaultUsername)
                .last("limit 1")
                .one();
        if (admin == null) {
            admin = new User();
            admin.setOpenid("local-admin-" + System.currentTimeMillis());
            admin.setNickname("默认超级管理员");
            admin.setAvatar("");
            admin.setMobile("");
            admin.setIsLeader(false);
            admin.setAdminRole("super_admin");
            admin.setLoginName(defaultUsername);
            admin.setPasswordHash(PasswordUtil.hash(defaultPassword));
            boolean saved = userService.save(admin);
            if (!saved) {
                return Result.error("默认管理员创建失败");
            }
            return Result.success("默认超级管理员已创建：admin / Admin@123456");
        }
        admin.setIsLeader(false);
        admin.setAdminRole("super_admin");
        admin.setLoginName(defaultUsername);
        admin.setPasswordHash(PasswordUtil.hash(defaultPassword));
        boolean updated = userService.updateById(admin);
        if (!updated) {
            return Result.error("默认管理员恢复失败");
        }
        return Result.success("默认超级管理员已恢复：admin / Admin@123456");
    }

    private Long resolveDevUserId(String code) {
        if (!StringUtils.hasText(code)) {
            return 1L;
        }
        String text = code.trim();
        if ("leader-test-code".equalsIgnoreCase(text)) {
            return userService.list().stream()
                    .filter(u -> Boolean.TRUE.equals(u.getIsLeader()))
                    .map(User::getId)
                    .findFirst()
                    .orElse(1L);
        }
        if ("admin-test-code".equalsIgnoreCase(text)) {
            return userService.list().stream()
                    .filter(u -> !Boolean.TRUE.equals(u.getIsLeader()))
                    .map(User::getId)
                    .findFirst()
                    .orElse(1L);
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            return 1L;
        }
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

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        return Result.success(user);
    }

    @PostMapping("/admin/create")
    public Result<User> createUser(@RequestBody UserManageReqDTO req) {
        if (!StringUtils.hasText(req.getNickname())) {
            return Result.error("昵称不能为空");
        }
        if (StringUtils.hasText(req.getMobile()) && !MOBILE_PATTERN.matcher(req.getMobile().trim()).matches()) {
            return Result.error("手机号格式错误");
        }

        User user = new User();
        user.setOpenid("dev-" + System.currentTimeMillis());
        user.setNickname(req.getNickname().trim());
        user.setAvatar(StringUtils.hasText(req.getAvatar()) ? req.getAvatar().trim() : "");
        user.setMobile(StringUtils.hasText(req.getMobile()) ? req.getMobile().trim() : "");
        user.setIsLeader(Boolean.TRUE.equals(req.getIsLeader()));
        if (Boolean.TRUE.equals(user.getIsLeader())) {
            user.setAdminRole("leader");
        } else {
            user.setAdminRole(StringUtils.hasText(req.getAdminRole()) ? req.getAdminRole().trim() : "ops_admin");
        }
        user.setLoginName(req.getMobile() == null ? "" : req.getMobile().trim());
        user.setPasswordHash(PasswordUtil.hash("Admin@123456"));

        boolean ok = userService.save(user);
        if (!ok) {
            return Result.error("用户创建失败");
        }
        return Result.success(user);
    }

    @PutMapping("/admin/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody UserManageReqDTO req) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        if (StringUtils.hasText(req.getNickname())) {
            user.setNickname(req.getNickname().trim());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar().trim());
        }
        if (req.getMobile() != null) {
            String mobile = req.getMobile().trim();
            if (StringUtils.hasText(mobile) && !MOBILE_PATTERN.matcher(mobile).matches()) {
                return Result.error("手机号格式错误");
            }
            user.setMobile(mobile);
        }
        if (req.getIsLeader() != null) {
            user.setIsLeader(req.getIsLeader());
            if (Boolean.TRUE.equals(req.getIsLeader())) {
                user.setAdminRole("leader");
            }
        }
        if (req.getAdminRole() != null && !Boolean.TRUE.equals(user.getIsLeader())) {
            user.setAdminRole(req.getAdminRole().trim());
        }
        if (req.getMobile() != null) {
            user.setLoginName(req.getMobile().trim());
        }

        boolean ok = userService.updateById(user);
        if (!ok) {
            return Result.error("用户更新失败");
        }
        return Result.success(user);
    }

    @DeleteMapping("/admin/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        boolean ok = userService.removeById(id);
        if (!ok) {
            return Result.error("用户删除失败");
        }
        return Result.success("删除成功");
    }

    @PutMapping("/leader/{id}")
    public Result<String> updateLeader(@PathVariable Long id, @RequestParam Boolean isLeader) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        user.setIsLeader(Boolean.TRUE.equals(isLeader));
        if (Boolean.TRUE.equals(isLeader)) {
            user.setAdminRole("leader");
        } else if (!StringUtils.hasText(user.getAdminRole()) || "leader".equalsIgnoreCase(user.getAdminRole())) {
            user.setAdminRole("ops_admin");
        }
        boolean ok = userService.updateById(user);
        if (!ok) {
            return Result.error("团长状态更新失败");
        }
        return Result.success("团长状态更新成功");
    }
}
