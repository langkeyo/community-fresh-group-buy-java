package com.langkeyo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupportTicketItemDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String contact;
    private Integer status;
    private String replyContent;
    private String replyBy;
    private LocalDateTime replyTime;
    private String assignedTo;
    private String closedBy;
    private LocalDateTime closedTime;
    private LocalDateTime lastMessageTime;
    private Integer unreadAdminCount;
    private Integer unreadUserCount;
    private LocalDateTime createTime;
}
