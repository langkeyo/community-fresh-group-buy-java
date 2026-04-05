package com.langkeyo.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.langkeyo.common.Result;
import com.langkeyo.dto.AiRecipeDTO;
import com.langkeyo.dto.AiRecipeStepDTO;
import com.langkeyo.dto.AiRecommendRespDTO;
import com.langkeyo.service.AiLlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiLlmService aiLlmService;

    @PostMapping("/recommend")
    public Result<AiRecommendRespDTO> recommend(@RequestParam String query) {
        // 1) 词典命中：直接返回DB结果
        if (hitDict(query, "番茄", "鸡蛋")) {
            AiRecipeStepDTO s1 = new AiRecipeStepDTO();
            s1.setStep(1);
            s1.setContent("番茄切块，鸡蛋打散");

            AiRecipeStepDTO s2 = new AiRecipeStepDTO();
            s2.setStep(2);
            s2.setContent("先炒鸡蛋盛出，再炒番茄出汁");

            AiRecipeStepDTO s3 = new AiRecipeStepDTO();
            s3.setStep(3);
            s3.setContent("回锅混炒，加盐调味即可");

            AiRecipeDTO recipe = new AiRecipeDTO();
            recipe.setTitle("番茄炒蛋（词典版）");
            recipe.setDesc("来自词典的稳定推荐结果");
            recipe.setTags(Arrays.asList("家常", "快手", "稳定命中"));
            recipe.setImage("https://loremflickr.com/500/300/tomato,egg?lock=301");
            recipe.setSteps(List.of(s1, s2, s3));

            AiRecommendRespDTO resp = new AiRecommendRespDTO();
            resp.setSource("DB");
            resp.setDisclaimer("该菜谱来自词典库，可放心参考");
            resp.setRecipe(recipe);
            return Result.success(resp);
        }

        JSONObject aiJson = aiLlmService.askRecipeJson(query);

        AiRecipeDTO recipe = new AiRecipeDTO();
        recipe.setTitle(aiJson.getStr("title", "AI推荐菜谱"));
        recipe.setDesc(aiJson.getStr("desc", "根据你的输入生成的推荐结果"));
        recipe.setTags(aiJson.getJSONArray("tags") == null
                ? Collections.emptyList()
                : aiJson.getJSONArray("tags").toList(String.class));
        recipe.setImage(aiJson.getStr("image", ""));

        List<AiRecipeStepDTO> steps = new ArrayList<>();
        JSONArray stepArr = aiJson.getJSONArray("steps");
        if (stepArr != null) {
            for (Object obj : stepArr) {
                JSONObject s = (JSONObject) obj;
                AiRecipeStepDTO step = new AiRecipeStepDTO();
                step.setStep(s.getInt("step", steps.size() + 1));
                step.setContent(s.getStr("content", ""));
                steps.add(step);
            }
        }
        recipe.setSteps(steps);

        AiRecommendRespDTO resp = new AiRecommendRespDTO();
        resp.setSource("AI");
        resp.setDisclaimer(aiJson.getStr("disclaimer", "该菜谱为AI生成，仅供参考，请结合实际食材调整"));
        resp.setRecipe(recipe);

        return Result.success(resp);
    }

    @PostMapping("/test-llm")
    public Result<String> testLlm(@RequestParam String query) {
        String content = aiLlmService.ask(query);
        return Result.success(content);
    }

    private boolean hitDict(String q, String... words) {
        if (q == null) return false;
        for (String w : words) {
            if (q.contains(w)) return true;
        }
        return false;
    }
}
