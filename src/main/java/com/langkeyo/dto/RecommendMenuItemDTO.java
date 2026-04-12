package com.langkeyo.dto;

import lombok.Data;

@Data
public class RecommendMenuItemDTO {
    private String name;
    private String value;
    private String icon;
    private String iconColor;
    private String bg;
    private String color;
    private Boolean enabled;
    private Integer sort;
}
