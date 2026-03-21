package com.langkeyo.common;

import lombok.Getter;

/**
 * 统一状态码枚举
 * @author langkeyo
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    // 用户相关 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_LOGIN_ERROR(1002, "登录失败"),
    USER_NOT_LOGIN(1003, "用户未登录"),
    TOKEN_INVALID(1004, "Token无效或已过期"),

    // 商品相关 2xxx
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_STOCK_NOT_ENOUGH(2002, "商品库存不足"),
    PRODUCT_OFF_SHELF(2003, "商品已下架"),

    // 订单相关 3xxx
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态错误"),
    ORDER_CREATE_FAIL(3003, "订单创建失败"),
    ORDER_UPDATE_FAIL(3004, "订单更新失败"),

    // 参数校验 4xxx
    PARAM_ERROR(4001, "参数错误"),
    PARAM_IS_BLANK(4002, "参数为空"),

    // 系统相关 5xxx
    SYSTEM_ERROR(5001, "系统异常"),
    UNAUTHORIZED(5002, "无权限访问");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}