package com.langkeyo.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiRecipeDTO {
    private String title;
    private String desc;
    private List<String> tags;
    private String image;
    private List<AiIngredientDTO> ingredients;
    private List<AiRecipeStepDTO> steps;
}
