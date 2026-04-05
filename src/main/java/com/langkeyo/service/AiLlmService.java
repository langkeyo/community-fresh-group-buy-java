package com.langkeyo.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.langkeyo.config.DeepSeekProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiLlmService {
    private final DeepSeekProperties deepSeekProperties;

    public String ask(String userPrompt) {
        if (deepSeekProperties.getApiKey() == null || deepSeekProperties.getApiKey().isEmpty()) {
            throw new RuntimeException("DEEPSEEK_API_KEY 未配置");
        }

        JSONObject body = new JSONObject();
        body.set("model", deepSeekProperties.getModel());

        JSONArray messages = new JSONArray();
        messages.add(new JSONObject()
                .set("role", "system")
                .set("content",
                        "你是生鲜食材推荐助手。请严格只返回JSON，不要返回markdown或解释。"
                                + "JSON结构必须是："
                                + "{"
                                + "\"title\":\"\","
                                + "\"desc\":\"\","
                                + "\"tags\":[\"\"],"
                                + "\"image\":\"\","
                                + "\"steps\":[{\"step\":1,\"content\":\"\"}],"
                                + "\"disclaimer\":\"该菜谱为AI生成，仅供参考，请结合实际食材调整\""
                                + "}"));
        messages.add(new JSONObject().set("role", "user").set("content", userPrompt));
        body.set("messages", messages);

        String url = deepSeekProperties.getBaseUrl() + "/chat/completions";
        String apiKey = deepSeekProperties.getApiKey() == null ? "" : deepSeekProperties.getApiKey().trim();

        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(deepSeekProperties.getTimeoutMs())
                .body(body.toString())
                .execute();

        if (response.getStatus() != 200) {
            throw new RuntimeException("DeepSeek 调用失败: HTTP " + response.getStatus());
        }

        JSONObject json = JSONUtil.parseObj(response.body());
        return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getStr("content");
    }

    private String extractJson(String content) {
        if (content == null) return "{}";
        String text = content.trim();
        if (text.startsWith("```")) {
            int first = text.indexOf("{");
            int last = text.lastIndexOf("}");
            if (first >= 0 && last > first) {
                return text.substring(first, last + 1);
            }
        }
        return text;
    }

    public JSONObject askRecipeJson(String userPrompt) {
        String raw = ask(userPrompt);
        String jsonText = extractJson(raw);
        try {
            return JSONUtil.parseObj(jsonText);
        } catch (Exception e) {
            throw new RuntimeException("AI返回不是合法JSON: " + jsonText);
        }
    }
}
