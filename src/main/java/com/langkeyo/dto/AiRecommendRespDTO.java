package com.langkeyo.dto;

import lombok.Data;

@Data
public class AiRecommendRespDTO {
    // DB 或 AI
    private String source;
    // AI 场景提示文案
    private String disclaimer;
    private AiRecipeDTO recipe;
}
