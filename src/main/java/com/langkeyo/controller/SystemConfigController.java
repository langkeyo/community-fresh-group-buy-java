package com.langkeyo.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.langkeyo.common.Result;
import com.langkeyo.dto.RecommendMenuItemDTO;
import com.langkeyo.dto.SystemConfigDTO;
import com.langkeyo.entity.SystemConfigStore;
import com.langkeyo.mapper.SystemConfigStoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/config")
public class SystemConfigController {

    private static final String CONFIG_KEY = "SYSTEM_CONFIG_MAIN";

    @Autowired
    private SystemConfigStoreMapper systemConfigStoreMapper;

    @GetMapping
    public Result<SystemConfigDTO> getConfig() {
        SystemConfigDTO dto = defaultConfig();
        SystemConfigStore store = getStoreByKey(CONFIG_KEY);
        if (store == null || !StringUtils.hasText(store.getConfigValue())) {
            return Result.success(dto);
        }

        JSONObject json = JSONUtil.parseObj(store.getConfigValue());
        String noticeText = json.getStr("noticeText");
        String servicePhone = json.getStr("servicePhone");
        String serviceWechat = json.getStr("serviceWechat");
        String serviceHours = json.getStr("serviceHours");
        String serviceTerms = json.getStr("serviceTerms");
        JSONArray menuArray = json.getJSONArray("recommendMenus");
        JSONObject extendedSettings = json.getJSONObject("extendedSettings");

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
        if (extendedSettings != null && !extendedSettings.isEmpty()) {
            dto.setExtendedSettings(JSONUtil.toBean(extendedSettings, Map.class));
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
        safe.setRecommendMenus(sanitizeRecommendMenus(req.getRecommendMenus()));
        safe.setExtendedSettings(sanitizeExtendedSettings(req.getExtendedSettings()));

        SystemConfigStore store = getStoreByKey(CONFIG_KEY);
        if (store == null) {
            store = new SystemConfigStore();
            store.setConfigKey(CONFIG_KEY);
            store.setConfigValue(JSONUtil.toJsonStr(safe));
            systemConfigStoreMapper.insert(store);
        } else {
            store.setConfigValue(JSONUtil.toJsonStr(safe));
            systemConfigStoreMapper.updateById(store);
        }
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
        dto.setExtendedSettings(defaultExtendedSettings());
        return dto;
    }

    private SystemConfigStore getStoreByKey(String key) {
        QueryWrapper<SystemConfigStore> wrapper = new QueryWrapper<>();
        wrapper.eq("config_key", key).last("limit 1");
        return systemConfigStoreMapper.selectOne(wrapper);
    }

    private Map<String, Object> sanitizeExtendedSettings(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return defaultExtendedSettings();
        }
        JSONObject merged = JSONUtil.parseObj(defaultExtendedSettings());
        merged.putAll(payload);
        return JSONUtil.toBean(merged, Map.class);
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

    private Map<String, Object> defaultExtendedSettings() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("roles", List.of(
                role("super-admin", "超级管理员", "全部站点", true, true, true),
                role("ops-manager", "运营主管", "负责区域", true, true, true),
                role("service-agent", "客服专员", "仅客服工单", false, false, true)
        ));
        root.put("menuPermissions", List.of());

        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("smsEnabled", true);
        notification.put("emailEnabled", false);
        notification.put("wechatEnabled", true);
        notification.put("quietHoursEnabled", true);
        notification.put("quietStart", "23:00");
        notification.put("quietEnd", "08:00");
        notification.put("severeAlertThreshold", 3);
        root.put("notification", notification);

        Map<String, Object> paymentShipping = new LinkedHashMap<>();
        paymentShipping.put("paymentProvider", "hybrid");
        paymentShipping.put("codEnabled", false);
        paymentShipping.put("autoRefundHours", 48);
        paymentShipping.put("fraudReviewEnabled", true);
        paymentShipping.put("shippingProvider", "self-built");
        paymentShipping.put("freeShippingThreshold", 59);
        paymentShipping.put("coldChainRequired", true);
        root.put("paymentShipping", paymentShipping);

        Map<String, Object> apiWebhook = new LinkedHashMap<>();
        apiWebhook.put("apiKey", "ak_live_A81FXXXXX9W");
        apiWebhook.put("apiSecret", "sk_live_XXXXXXXXXXXXXXXX");
        apiWebhook.put("webhookEnabled", true);
        apiWebhook.put("webhookUrl", "");
        apiWebhook.put("webhookSecret", "whsec_XXXXXXXXXXXXXXX");
        apiWebhook.put("webhookRetry", 3);
        root.put("apiWebhook", apiWebhook);

        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("retentionDays", 180);
        audit.put("tamperProof", true);
        audit.put("rows", List.of(
                auditRow("L-001", "角色权限变更", "李娜", "warning", "2026-04-26 10:31"),
                auditRow("L-002", "Webhook Secret 轮换", "系统管理员", "critical", "2026-04-25 18:07"),
                auditRow("L-003", "通知规则调整", "王伟", "info", "2026-04-24 14:53")
        ));
        root.put("audit", audit);

        Map<String, Object> security = new LinkedHashMap<>();
        security.put("maintenanceMode", false);
        security.put("forceMfaForAdmins", true);
        security.put("blockUnknownIpLogin", true);
        root.put("security", security);

        root.put("homeBanners", List.of(
                banner("今日新鲜直供", "", 1L, true, 1, "限时"),
                banner("2人拼更划算", "", 2L, true, 2, "爆款")
        ));
        return root;
    }

    private Map<String, Object> role(String id, String name, String dataScope, boolean canExport, boolean mfaRequired, boolean enabled) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("dataScope", dataScope);
        item.put("canExport", canExport);
        item.put("mfaRequired", mfaRequired);
        item.put("enabled", enabled);
        return item;
    }

    private Map<String, Object> auditRow(String id, String action, String operator, String level, String at) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("action", action);
        item.put("operator", operator);
        item.put("level", level);
        item.put("at", at);
        return item;
    }

    private Map<String, Object> banner(String title, String imageUrl, Long productId, boolean enabled, int sort, String badgeText) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("imageUrl", imageUrl);
        item.put("productId", productId);
        item.put("enabled", enabled);
        item.put("sort", sort);
        item.put("badgeText", badgeText);
        return item;
    }
}
