package com.langkeyo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupportMessageItemDTO {
    private Long id;
    private Long ticketId;
    private String senderType;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime createTime;
}
