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
            throw new RuntimeException("AI服务未配置，请联系管理员");
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
                                + "\"ingredients\":[{\"name\":\"\",\"amount\":\"\",\"unit\":\"\"}],"
                                + "\"steps\":[{\"step\":1,\"content\":\"\"}],"
                                + "\"disclaimer\":\"该菜谱为AI生成，仅供参考，请结合实际食材调整\""
                                + "}。"
                                + "image字段必须返回可直接访问的https图片直链，优先使用loremflickr，格式示例："
                                + "https://loremflickr.com/800/600/food,{关键词}?lock={1-9999整数}。"
                                + "禁止返回网页链接、相对路径、base64或需要鉴权的地址。"
                ));
        messages.add(new JSONObject().set("role", "user").set("content", userPrompt));
        body.set("messages", messages);

        String url = deepSeekProperties.getBaseUrl() + "/chat/completions";
        String apiKey = deepSeekProperties.getApiKey() == null ? "" : deepSeekProperties.getApiKey().trim();

        try {
            HttpResponse response = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(deepSeekProperties.getTimeoutMs())
                    .body(body.toString())
                    .execute();

            int status = response.getStatus();
            if (status != 200) {
                throw new RuntimeException(mapHttpStatusMessage(status));
            }

            JSONObject json = JSONUtil.parseObj(response.body());
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");
        } catch (RuntimeException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("AI服务")) {
                throw e;
            }
            throw new RuntimeException(mapExceptionMessage(e));
        } catch (Exception e) {
            throw new RuntimeException(mapExceptionMessage(e));
        }
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

    private String mapHttpStatusMessage(int status) {
        if (status == 401 || status == 403) {
            return "AI服务鉴权失败，请联系管理员";
        }
        if (status >= 500) {
            return "AI服务暂时不可用，请稍后重试";
        }
        return "AI服务请求失败，请稍后重试";
    }

    private String mapExceptionMessage(Exception e) {
        String text = e == null || e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (text.contains("timeout") || text.contains("timed out") || text.contains("read timed out")) {
            return "AI服务响应超时，请稍后重试";
        }
        return "AI服务请求失败，请稍后重试";
    }
}
