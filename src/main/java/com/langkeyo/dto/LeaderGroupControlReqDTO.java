package com.langkeyo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaderGroupControlReqDTO {
    private Integer groupOpen;
    private LocalDateTime groupStartTime;
    private LocalDateTime groupEndTime;
}
