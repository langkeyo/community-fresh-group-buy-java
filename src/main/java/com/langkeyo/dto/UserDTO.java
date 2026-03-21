package com.langkeyo.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String nickname;
    private String avatar;
    private Boolean isLeader;
    private String mobile;
}
