package com.langkeyo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupportActionLogItemDTO {
    private Long id;
    private Long ticketId;
    private String actionType;
    private String operator;
    private String detail;
    private LocalDateTime createTime;
}
