package com.langkeyo.dto;

import lombok.Data;

@Data
public class UserManageReqDTO {
    private String nickname;
    private String avatar;
    private String mobile;
    private Boolean isLeader;
    private String adminRole;
}
