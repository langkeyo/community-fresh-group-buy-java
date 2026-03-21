package com.langkeyo.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private UserDTO userInfo;
}
