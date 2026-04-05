// AiRecommendReview.java
package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_recommend_review")
public class AiRecommendReview {
    @TableId
    private Long id;
    private String queryText;
    private String source;
    private String recipeJson;
    private String status;
    private String reviewer;
    private String reviewRemark;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
