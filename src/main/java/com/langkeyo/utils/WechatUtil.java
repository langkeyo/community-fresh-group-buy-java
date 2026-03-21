package com.langkeyo.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 微信API工具类
 * @author langkeyo
 */
@Slf4j
@Component
public class WechatUtil {

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    /**
     * 微信登录接口地址
     */
    private static final String JSCODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    /**
     * 通过code获取openid和session_key
     * @param code 小程序端调用wx.login()获取的code
     * @return openid
     */
    public String getOpenid(String code) {
        try {
            String url = String.format(JSCODE2SESSION_URL, appid, secret, code);
            String response = HttpUtil.get(url);

            log.info("微信登录返回: {}", response);

            JSONObject jsonObject = JSONUtil.parseObj(response);

            // 检查是否有错误
            if (jsonObject.containsKey("errcode")) {
                Integer errcode = jsonObject.getInt("errcode");
                if (errcode != 0) {
                    String errmsg = jsonObject.getStr("errmsg");
                    log.error("微信登录失败: errcode={}, errmsg={}", errcode, errmsg);
                    throw new RuntimeException("微信登录失败: " + errmsg);
                }
            }

            return jsonObject.getStr("openid");
        } catch (Exception e) {
            log.error("调用微信API异常", e);
            throw new RuntimeException("微信登录失败，请稍后重试");
        }
    }
}