package com.langkeyo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SystemConfigDTO {
    private String noticeText;
    private String servicePhone;
    private String serviceWechat;
    private String serviceHours;
    private String serviceTerms;
    private List<RecommendMenuItemDTO> recommendMenus = new ArrayList<>();
    private Map<String, Object> extendedSettings;
}
