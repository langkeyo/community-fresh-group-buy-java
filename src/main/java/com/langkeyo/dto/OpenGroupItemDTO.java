package com.langkeyo.dto;

import lombok.Data;

@Data
public class OpenGroupItemDTO {
    private String groupBuyId;
    private Integer currentCount;
    private Integer targetCount;
    private String latestCreateTime;
}

