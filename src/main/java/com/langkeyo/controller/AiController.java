package com.langkeyo.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.langkeyo.common.Result;
import com.langkeyo.dto.AiRecipeDTO;
import com.langkeyo.dto.AiRecipeStepDTO;
import com.langkeyo.dto.AiRecommendRespDTO;
import com.langkeyo.entity.AiRecipeLibrary;
import com.langkeyo.entity.AiRecommendReview;
import com.langkeyo.mapper.AiRecipeLibraryMapper;
import com.langkeyo.mapper.AiRecommendReviewMapper;
import com.langkeyo.service.AiLlmService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    private final AiLlmService aiLlmService;
    private final AiRecommendReviewMapper aiRecommendReviewMapper;
    private final AiRecipeLibraryMapper aiRecipeLibraryMapper;
    private final Environment environment;

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

        JSONObject recipeObj = aiJson.getJSONObject("recipe");
        // 兼容两种模型输出：
        // 1) 嵌套：{ recipe: {...} }
        // 2) 扁平：{ title, desc, tags, image, steps, disclaimer }
        if (recipeObj == null || recipeObj.isEmpty()) {
            recipeObj = new JSONObject();
            recipeObj.set("title", aiJson.getStr("title", ""));
            recipeObj.set("desc", aiJson.getStr("desc", ""));
            recipeObj.set("tags", aiJson.getJSONArray("tags"));
            recipeObj.set("image", aiJson.getStr("image", ""));
            recipeObj.set("steps", aiJson.getJSONArray("steps"));
        }
        String recipeJson = recipeObj.toString();
        log.info("recipeJson={}", recipeJson);

        AiRecommendReview review = new AiRecommendReview();
        review.setQueryText(query);
        review.setSource("AI");
        review.setRecipeJson(recipeJson);
        review.setStatus("PENDING");
        review.setCreatedAt(LocalDateTime.now());
        aiRecommendReviewMapper.insert(review);

        resp.setDisclaimer(aiJson.getStr("disclaimer", "该菜谱为AI生成，仅供参考，请结合实际食材调整"));
        resp.setRecipe(recipe);

        return Result.success(resp);
    }

    @GetMapping("/review/list")
    public Result<List<AiRecommendReview>> reviewList(@RequestParam(defaultValue = "PENDING") String status) {
        LambdaQueryWrapper<AiRecommendReview> qw = new LambdaQueryWrapper<>();
        qw.eq(AiRecommendReview::getStatus, status).orderByDesc(AiRecommendReview::getId);
        return Result.success(aiRecommendReviewMapper.selectList(qw));
    }

    @GetMapping("/review/list/page")
    public Result<Page<AiRecommendReview>> reviewListPage(@RequestParam(defaultValue = "PENDING") String status,
                                                          @RequestParam(defaultValue = "1") long page,
                                                          @RequestParam(defaultValue = "10") long size) {
        long safePage = page < 1 ? 1 : page;
        long safeSize = size < 1 ? 10 : Math.min(size, 100);
        LambdaQueryWrapper<AiRecommendReview> qw = new LambdaQueryWrapper<>();
        qw.eq(AiRecommendReview::getStatus, status).orderByDesc(AiRecommendReview::getId);
        Page<AiRecommendReview> p = new Page<>(safePage, safeSize);
        aiRecommendReviewMapper.selectPage(p, qw);
        return Result.success(p);
    }

    @PutMapping("/review/approve/{id}")
    public Result<String> approve(@PathVariable Long id, @RequestParam(required = false) String reviewer) {
        AiRecommendReview row = aiRecommendReviewMapper.selectById(id);
        if (row == null) return Result.error("记录不存在");
        String reviewerName = reviewer == null || reviewer.trim().isEmpty() ? "admin" : reviewer.trim();
        if (!"APPROVED".equals(row.getStatus())) {
            row.setStatus("APPROVED");
            row.setReviewer(reviewerName);
            row.setReviewedAt(LocalDateTime.now());
            aiRecommendReviewMapper.updateById(row);
        }

        ensureLibraryRow(row);
        return Result.success("审核通过");
    }

    @PutMapping("/review/reject/{id}")
    public Result<String> reject(@PathVariable Long id, @RequestParam(required = false) String reviewer, @RequestParam(required = false) String remark) {
        AiRecommendReview row = aiRecommendReviewMapper.selectById(id);
        if (row == null) return Result.error("记录不存在");
        String safeRemark = remark == null ? "" : remark.trim();
        if (safeRemark.length() < 2) {
            return Result.error("驳回原因至少2个字");
        }
        row.setStatus("REJECTED");
        row.setReviewer(reviewer == null || reviewer.trim().isEmpty() ? "admin" : reviewer.trim());
        row.setReviewRemark(safeRemark);
        row.setReviewedAt(LocalDateTime.now());
        aiRecommendReviewMapper.updateById(row);
        return Result.success("已驳回");
    }

    @PostMapping("/review/regenerate/{id}")
    public Result<String> regenerate(@PathVariable Long id, @RequestParam(required = false) String reviewer) {
        AiRecommendReview row = aiRecommendReviewMapper.selectById(id);
        if (row == null) return Result.error("记录不存在");
        if (!"REJECTED".equals(row.getStatus())) {
            return Result.error("仅支持驳回记录重新生成");
        }

        JSONObject aiJson = aiLlmService.askRecipeJson(row.getQueryText());
        JSONObject recipeObj = aiJson.getJSONObject("recipe");
        if (recipeObj == null || recipeObj.isEmpty()) {
            recipeObj = new JSONObject();
            recipeObj.set("title", aiJson.getStr("title", ""));
            recipeObj.set("desc", aiJson.getStr("desc", ""));
            recipeObj.set("tags", aiJson.getJSONArray("tags"));
            recipeObj.set("image", aiJson.getStr("image", ""));
            recipeObj.set("steps", aiJson.getJSONArray("steps"));
        }

        row.setRecipeJson(recipeObj.toString());
        row.setStatus("PENDING");
        row.setReviewer(reviewer == null || reviewer.trim().isEmpty() ? "admin" : reviewer.trim());
        row.setReviewRemark(null);
        row.setReviewedAt(null);
        aiRecommendReviewMapper.updateById(row);
        return Result.success("已重新生成");
    }

    @PostMapping("/test-llm")
    public Result<String> testLlm(@RequestParam String query) {
        if (!isDevProfile()) {
            return Result.error("该接口仅开发环境可用");
        }
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

    private void ensureLibraryRow(AiRecommendReview row) {
        JSONObject recipeObj = JSONUtil.parseObj(row.getRecipeJson());
        String title = recipeObj.getStr("title", "未命名菜谱");
        String normalizedQuery = normalizeQuery(row.getQueryText());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(10);

        LambdaQueryWrapper<AiRecipeLibrary> qw = new LambdaQueryWrapper<>();
        qw.eq(AiRecipeLibrary::getQueryText, normalizedQuery)
                .eq(AiRecipeLibrary::getTitle, title)
                .ge(AiRecipeLibrary::getCreatedAt, windowStart)
                .last("limit 1");
        AiRecipeLibrary existing = aiRecipeLibraryMapper.selectOne(qw);
        if (existing != null) {
            return;
        }

        AiRecipeLibrary lib = new AiRecipeLibrary();
        lib.setQueryText(normalizedQuery);
        lib.setTitle(title);
        lib.setTagsJson(JSONUtil.toJsonStr(recipeObj.get("tags")));
        lib.setRecipeJson(row.getRecipeJson());
        lib.setHitCount(0);
        lib.setSource(row.getSource());
        lib.setCreatedAt(now);
        aiRecipeLibraryMapper.insert(lib);
    }

    private String normalizeQuery(String text) {
        if (text == null) return "";
        String trimmed = text.trim().toLowerCase(Locale.ROOT);
        return trimmed.replaceAll("\\s+", " ");
    }

    private boolean isDevProfile() {
        String[] profiles = environment.getActiveProfiles();
        for (String p : profiles) {
            if ("dev".equalsIgnoreCase(p) || "local".equalsIgnoreCase(p)) {
                return true;
            }
        }
        return false;
    }
}
