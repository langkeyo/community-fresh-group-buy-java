package com.langkeyo.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.langkeyo.common.Result;
import com.langkeyo.dto.RecommendMenuItemDTO;
import com.langkeyo.dto.SystemConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        JSONArray menuArray = json.getJSONArray("recommendMenus");

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
        dto.setRecommendMenus(resolveRecommendMenus(menuArray));

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
        safe.setRecommendMenus(sanitizeRecommendMenus(req.getRecommendMenus()));
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
        dto.setRecommendMenus(defaultRecommendMenus());
        return dto;
    }

    private List<RecommendMenuItemDTO> resolveRecommendMenus(JSONArray menuArray) {
        if (menuArray == null || menuArray.isEmpty()) {
            return defaultRecommendMenus();
        }
        List<RecommendMenuItemDTO> list = new ArrayList<>();
        for (Object obj : menuArray) {
            JSONObject item = JSONUtil.parseObj(obj);
            RecommendMenuItemDTO row = new RecommendMenuItemDTO();
            row.setName(item.getStr("name"));
            row.setValue(item.getStr("value"));
            row.setIcon(item.getStr("icon"));
            row.setIconColor(item.getStr("iconColor"));
            row.setBg(item.getStr("bg"));
            row.setColor(item.getStr("color"));
            row.setEnabled(item.getBool("enabled", true));
            row.setSort(item.getInt("sort", 999));
            list.add(row);
        }
        return sanitizeRecommendMenus(list);
    }

    private List<RecommendMenuItemDTO> sanitizeRecommendMenus(List<RecommendMenuItemDTO> menus) {
        List<RecommendMenuItemDTO> fallback = defaultRecommendMenus();
        if (menus == null || menus.isEmpty()) {
            return fallback;
        }

        List<RecommendMenuItemDTO> safe = new ArrayList<>();
        for (RecommendMenuItemDTO item : menus) {
            if (item == null || !StringUtils.hasText(item.getName()) || !StringUtils.hasText(item.getValue())) {
                continue;
            }
            RecommendMenuItemDTO row = new RecommendMenuItemDTO();
            row.setName(item.getName().trim());
            row.setValue(item.getValue().trim());
            row.setIcon(StringUtils.hasText(item.getIcon()) ? item.getIcon().trim() : "fire-filled");
            row.setIconColor(StringUtils.hasText(item.getIconColor()) ? item.getIconColor().trim() : "#F08800");
            row.setBg(StringUtils.hasText(item.getBg()) ? item.getBg().trim() : "bg-orange-100");
            row.setColor(StringUtils.hasText(item.getColor()) ? item.getColor().trim() : "text-orange-600");
            row.setEnabled(item.getEnabled() == null || item.getEnabled());
            row.setSort(item.getSort() == null ? 999 : item.getSort());
            safe.add(row);
        }

        if (safe.isEmpty()) {
            return fallback;
        }

        safe.sort(Comparator.comparingInt(i -> i.getSort() == null ? 999 : i.getSort()));
        return safe;
    }

    private List<RecommendMenuItemDTO> defaultRecommendMenus() {
        List<RecommendMenuItemDTO> list = new ArrayList<>();
        list.add(buildMenu("蔬菜", "vegetable", "fire-filled", "#16a34a", "bg-green-100", "text-green-600", true, 1));
        list.add(buildMenu("水果", "fruit", "gift-filled", "#dc2626", "bg-red-100", "text-red-600", true, 2));
        list.add(buildMenu("肉蛋", "meat", "cart-filled", "#ea580c", "bg-orange-100", "text-orange-600", true, 3));
        list.add(buildMenu("海鲜", "seafood", "flag-filled", "#2563eb", "bg-blue-100", "text-blue-600", true, 4));
        return list;
    }

    private RecommendMenuItemDTO buildMenu(String name, String value, String icon, String iconColor,
                                           String bg, String color, boolean enabled, int sort) {
        RecommendMenuItemDTO row = new RecommendMenuItemDTO();
        row.setName(name);
        row.setValue(value);
        row.setIcon(icon);
        row.setIconColor(iconColor);
        row.setBg(bg);
        row.setColor(color);
        row.setEnabled(enabled);
        row.setSort(sort);
        return row;
    }
}
