// AiRecipeLibrary.java
package com.langkeyo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_recipe_library")
public class AiRecipeLibrary {
    @TableId
    private Long id;
    private String queryText;
    private String title;
    private String tagsJson;
    private String recipeJson;
    private String source;
    private LocalDateTime createdAt;
}
