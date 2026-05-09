package com.langkeyo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeItemDTO {
    private Long id;
    private String title;
    private String content;
    private Integer status;
    private Boolean read;
    private LocalDateTime createTime;
}

