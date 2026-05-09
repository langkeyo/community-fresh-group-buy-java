package com.langkeyo.controller;

import com.langkeyo.common.Result;
import com.langkeyo.dto.LoginResponseDTO;
import com.langkeyo.entity.User;
import com.langkeyo.service.IUserService;
import com.langkeyo.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private IUserService userService;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController userController;

    @Test
    void dev_login_supports_admin_test_code() {
        User user = new User();
        user.setId(1L);
        user.setOpenid("dev-openid");
        when(userService.getById(1L)).thenReturn(user);
        when(jwtUtil.generateToken(1L, "dev-openid")).thenReturn("token-dev");

        Result<LoginResponseDTO> result = userController.devLogin(null, "admin-test-code");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("token-dev", result.getData().getToken());
    }
}
