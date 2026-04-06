package com.langkeyo.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.langkeyo.common.Result;
import com.langkeyo.dto.SystemConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/config")
public class SystemConfigController {

    private static final String CONFIG_KEY = "SYSTEM:CONFIG:BASIC";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping
    public Result<SystemConfigDTO> getConfig() {
        SystemConfigDTO dto = defaultConfig();
        Object raw = redisTemplate.opsForValue().get(CONFIG_KEY);
        if (raw == null) {
            return Result.success(dto);
        }

        JSONObject json = JSONUtil.parseObj(raw);
        String noticeText = json.getStr("noticeText");
        String servicePhone = json.getStr("servicePhone");
        String serviceWechat = json.getStr("serviceWechat");
        String serviceHours = json.getStr("serviceHours");
        String serviceTerms = json.getStr("serviceTerms");

        if (StringUtils.hasText(noticeText)) {
            dto.setNoticeText(noticeText);
        }
        if (StringUtils.hasText(servicePhone)) {
            dto.setServicePhone(servicePhone);
        }
        if (StringUtils.hasText(serviceWechat)) {
            dto.setServiceWechat(serviceWechat);
        }
        if (StringUtils.hasText(serviceHours)) {
            dto.setServiceHours(serviceHours);
        }
        if (StringUtils.hasText(serviceTerms)) {
            dto.setServiceTerms(serviceTerms);
        }

        return Result.success(dto);
    }

    @PutMapping
    public Result<String> saveConfig(@RequestBody SystemConfigDTO req) {
        SystemConfigDTO safe = defaultConfig();
        if (StringUtils.hasText(req.getNoticeText())) {
            safe.setNoticeText(req.getNoticeText().trim());
        }
        if (StringUtils.hasText(req.getServicePhone())) {
            safe.setServicePhone(req.getServicePhone().trim());
        }
        if (StringUtils.hasText(req.getServiceWechat())) {
            safe.setServiceWechat(req.getServiceWechat().trim());
        }
        if (StringUtils.hasText(req.getServiceHours())) {
            safe.setServiceHours(req.getServiceHours().trim());
        }
        if (StringUtils.hasText(req.getServiceTerms())) {
            safe.setServiceTerms(req.getServiceTerms().trim());
        }
        redisTemplate.opsForValue().set(CONFIG_KEY, JSONUtil.toJsonStr(safe));
        return Result.success("配置保存成功");
    }

    private SystemConfigDTO defaultConfig() {
        SystemConfigDTO dto = new SystemConfigDTO();
        dto.setNoticeText("欢迎来到社区团购，今天也有新鲜直供好货。");
        dto.setServicePhone("400-800-1234");
        dto.setServiceWechat("ligo-service");
        dto.setServiceHours("09:00-22:00");
        dto.setServiceTerms("服务条款：当前版本为毕业设计演示环境，支付流程为模拟链路。");
        return dto;
    }
}
