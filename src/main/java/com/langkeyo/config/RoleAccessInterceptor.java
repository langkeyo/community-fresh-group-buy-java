package com.langkeyo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langkeyo.common.Result;
import com.langkeyo.common.ResultCode;
import com.langkeyo.entity.User;
import com.langkeyo.service.IUserService;
import com.langkeyo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleAccessInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final IUserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RoleAccessInterceptor(JwtUtil jwtUtil, IUserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        boolean leaderOnly = path.startsWith("/api/order/leader/") || path.startsWith("/api/product/leader/");
        boolean supportAdmin = path.startsWith("/api/support/admin/");
        boolean adminOnly = (path.contains("/admin/") || path.startsWith("/api/user/leader/")) && !supportAdmin;
        boolean superAdminOnly = path.startsWith("/api/user/admin/")
                || path.startsWith("/api/system-config/");
        if (!leaderOnly && !adminOnly && !superAdminOnly && !supportAdmin) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth)) {
            writeUnauthorized(response, ResultCode.USER_NOT_LOGIN.getMessage());
            return false;
        }
        String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
        if (!jwtUtil.validateToken(token)) {
            writeUnauthorized(response, ResultCode.TOKEN_INVALID.getMessage());
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getById(userId);
        if (user == null) {
            writeUnauthorized(response, ResultCode.USER_NOT_FOUND.getMessage());
            return false;
        }
        String adminRole = user.getAdminRole();
        if (!StringUtils.hasText(adminRole)) {
            adminRole = Boolean.TRUE.equals(user.getIsLeader()) ? "leader" : "super_admin";
        }
        boolean isLeader = "leader".equalsIgnoreCase(adminRole);
        if (leaderOnly && !isLeader) {
            writeUnauthorized(response, "仅团长可访问");
            return false;
        }
        if (adminOnly && isLeader) {
            writeUnauthorized(response, "仅管理员可访问");
            return false;
        }
        if (supportAdmin) {
            boolean supportAllowed = "super_admin".equalsIgnoreCase(adminRole) || "cs_agent".equalsIgnoreCase(adminRole);
            if (!supportAllowed) {
                writeUnauthorized(response, "仅客服或超级管理员可访问");
                return false;
            }
        }
        if (superAdminOnly && !"super_admin".equalsIgnoreCase(adminRole)) {
            writeUnauthorized(response, "仅超级管理员可访问");
            return false;
        }
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(message)));
    }
}
